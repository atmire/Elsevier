/**
 * Created by Apache CXF WadlToJava code generator
**/
package com.atmire.wadl;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("scopus_id:{scopus_id}")
public interface ScopusidResource {

    @GET
    @Produces("text/xml")
    Response simple(@HeaderParam("Accept") @DefaultValue("text/xml") String accept, @HeaderParam("Authorization") String authorization, @HeaderParam("X-ELS-APIKey") String x_ELS_APIKey, @HeaderParam("X-ELS-Authtoken") String x_ELS_Authtoken, 
                @HeaderParam("X-ELS-ReqId") String x_ELS_ReqId, @HeaderParam("X-ELS-ResourceVersion") String x_ELS_ResourceVersion, @QueryParam("httpAccept") String httpAccept, @QueryParam("access_token") String access_token, 
                @QueryParam("apiKey") String apiKey, @QueryParam("reqId") String reqId, @QueryParam("ver") String ver, @QueryParam("view") @DefaultValue("META") String view, 
                @QueryParam("field") String field, @QueryParam("startref") String startref, @QueryParam("refcount") String refcount, @PathParam("scopus_id") String scopus_id);

}