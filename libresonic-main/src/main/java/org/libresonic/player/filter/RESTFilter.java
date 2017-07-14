/*
 This file is part of Libresonic.

 Libresonic is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Libresonic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.

 Copyright 2016 (C) Libresonic Authors
 Based upon Subsonic, Copyright 2009 (C) Sindre Mehus
 */
package org.libresonic.player.filter;

import org.libresonic.player.Logger;
import org.libresonic.player.controller.JAXBWriter;
import org.libresonic.player.controller.RESTController;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.util.NestedServletException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.libresonic.player.controller.RESTController.ErrorCode.GENERIC;
import static org.libresonic.player.controller.RESTController.ErrorCode.MISSING_PARAMETER;

/**
 * Intercepts exceptions thrown by RESTController.
 *
 * Also adds the CORS response header (http://enable-cors.org)
 *
 * @author Sindre Mehus
 * @version $Revision: 1.1 $ $Date: 2006/03/01 16:58:08 $
 */
public class RESTFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(RESTFilter.class);

    private final JAXBWriter jaxbWriter = new JAXBWriter();

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        try {
            HttpServletResponse response = (HttpServletResponse) res;
            response.setHeader("Access-Control-Allow-Origin", "*");
            chain.doFilter(req, res);
        } catch (Throwable x) {
            handleException(x, (HttpServletRequest) req, (HttpServletResponse) res);
        }
    }

    private void handleException(Throwable x, HttpServletRequest request, HttpServletResponse response) {
        if (x instanceof NestedServletException && x.getCause() != null) {
            x = x.getCause();
        }

        RESTController.ErrorCode code = (x instanceof ServletRequestBindingException) ? MISSING_PARAMETER : GENERIC;
        String msg = getErrorMessage(x);
        LOG.warn("Error in REST API: " + msg, x);

        try {
            jaxbWriter.writeErrorResponse(request, response, code, msg);
        } catch (Exception e) {
            LOG.error("Failed to write error response.", e);
        }
    }

    private String getErrorMessage(Throwable x) {
        if (x.getMessage() != null) {
            return x.getMessage();
        }
        return x.getClass().getSimpleName();
    }

    public void init(FilterConfig filterConfig) {
    }

    public void destroy() {
    }
}