import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

class RequestHandler {

    static String handle(HashMap<String, String> headers, JSONObject body) {
        try {
            String type = body.getString("type");
            switch (type) {
                default: throw new RequestHandlingException(Status.BAD_REQUEST, "Unrecognised \"type\" parameter in body");
            }
        } catch (JSONException e) {
            return ErrorHandler.handle(new RequestHandlingException(Status.BAD_REQUEST, "Expected \"type\" parameter in body"));
        } catch (RequestHandlingException e) {
            return ErrorHandler.handle(e);
        }
    }
}
