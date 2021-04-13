package com.fr31b3u73r.jodel;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.Proxy;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class JodelHttpAction {
    JsonObject locationObject = new JsonObject();
    Proxy proxy = null;
    String accessToken = null;
    String deviceUID = null;
    String expirationDate = null;
    String distinctID = null;
    String refreshToken = null;
    String latitude = null;
    String longitude = null;
    Locale country;

    protected JodelHttpAction() {
    }

    private static String getDataString(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()));
            result.append("=");
            result.append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8.name()));
        }
        return result.toString();
    }

    private static byte[] readStreamToEnd(final InputStream is) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (is != null) {
            final byte[] buff = new byte[1024];
            int read;
            do {
                bos.write(buff, 0, (read = is.read(buff)) < 0 ? 0 : read);
            } while (read >= 0);
            bos.flush();
            is.close();
        }
        return bos.toByteArray();
    }

    protected void updateAccessValues(Proxy proxy, JsonObject locationObject, String accessToken, String distinctID, String refreshToken, String deviceUID, String expirationDate, String latitude, String longitude, Locale country) {
        this.proxy = proxy;
        this.locationObject = locationObject;
        this.accessToken = accessToken;
        this.deviceUID = deviceUID;
        this.expirationDate = expirationDate;
        this.distinctID = distinctID;
        this.refreshToken = refreshToken;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
    }

    protected JodelHttpResponse setLocation() {
        JsonObject payload = new JsonObject();
        payload.add("location", this.locationObject);
        return this.sendRequest("PUT", "/v2/users/location", payload);
    }

    protected JodelHttpResponse setPin(String postID) {
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/pin", null);
    }

    protected JodelHttpResponse setNotificationReadPostID(String postID) {
        return this.sendRequest("PUT", "/v3/user/notifications/post/" + postID + "/read", null);
    }

    protected JodelHttpResponse setNotificationReadNotificationID(String notificationID) {
        return this.sendRequest("PUT", "/v3/user/notifications/" + notificationID + "/read", null);
    }

    protected JodelHttpResponse setUserProfile(String userType, String gender, int age) {
        JsonObject payload = new JsonObject();
        payload.addProperty("user_type", userType);
        payload.addProperty("gender", gender);
        payload.addProperty("age", age);
        return this.sendRequest("PUT", "/v3/user/profile", payload);
    }

    protected JodelHttpResponse setUserLanguage(Locale locale) {
        JsonObject payload = new JsonObject();
        payload.addProperty("language", locale.toLanguageTag().toLowerCase());
        return this.sendRequest("PUT", "/v3/user/language", payload);
    }

    protected JodelHttpResponse removePin(String postID) {
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/unpin", null);
    }

    protected JodelHttpResponse removeJodel(String postID) {
        return this.sendRequest("DELETE", "/v2/posts/" + postID, null);
    }

    protected JodelHttpResponse enableNotify(String postID) {
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/notifications/enable", null);
    }

    protected JodelHttpResponse disableNotify(String postID) {
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/notifications/disable", null);
    }

    protected JodelHttpResponse getUserConfig() {
        return this.sendRequest("GET", "/v3/user/config", null);
    }

    protected JodelHttpResponse getNewAccessToken() {
        JsonObject payload = new JsonObject();
        payload.addProperty("client_id", JodelAccount.CLIENT_ID);
        payload.addProperty("distinct_id", this.distinctID);
        payload.addProperty("refresh_token", this.refreshToken);
        return this.sendRequest("POST", "/v2/users/refreshToken", payload);
    }

    protected JodelHttpResponse getNewTokens() {
        JsonObject payload = new JsonObject();
        payload.addProperty("device_uid", this.deviceUID);
        payload.add("location", this.locationObject);
        payload.addProperty("client_id", JodelAccount.CLIENT_ID);
        payload.addProperty("language", country.toLanguageTag());
        payload.addProperty("iid", "");
        JsonObject regData = new JsonObject();
        payload.addProperty("channel", "");
        payload.addProperty("provider", "branch.io");
        payload.addProperty("campaign", "");
        payload.addProperty("feature", "");
        payload.addProperty("referrer_branch_id", "");
        payload.addProperty("referrer_id", "");
        payload.add("registration_data", regData);
        return this.sendRequest("POST", "/v2/users", payload);
    }

    protected JodelHttpResponse getCaptcha() {
        return this.sendRequest("GET", "/v3/user/verification/imageCaptcha", null);
    }

    protected JodelHttpResponse sendPushToken(String pushToken) {
        JsonObject payload = new JsonObject();
        payload.addProperty("push_token", pushToken);
        payload.addProperty("client_id", JodelAccount.CLIENT_ID);
        return this.sendRequest("PUT", "/v2/users/pushToken", payload);
    }

    protected JodelHttpResponse verifyPush(String verificationCode, long serverTime) {
        JsonObject payload = new JsonObject();
        payload.addProperty("verification_code", verificationCode);
        payload.addProperty("server_time", serverTime);
        return this.sendRequest("POST", "/v3/user/verification/push", payload);
    }

    protected JodelHttpResponse getNotifications() {
        return this.sendRequest("PUT", "/v3/user/notifications", null);
    }

    protected JodelHttpResponse getNotificationsNew() {
        return this.sendRequest("GET", "/v3/user/notifications/new", null);
    }

    protected JodelHttpResponse getJodels(String url, int page, String distance, String after, String hashtag, String channel, boolean home, boolean skipHometown) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("lat", this.latitude);
        parameters.put("lng", this.longitude);
//        parameters.put("feed_token", "");
        parameters.put("distance", distance != null ? distance : ""/*"dynamic"*/);
        parameters.put("page", page);
        parameters.put("home", home);
        parameters.put("skipHometown", skipHometown);
//        parameters.put("skip", skip);
//        parameters.put("limit", limit);
        if (hashtag != null) {
            parameters.put("hashtag", hashtag);
        }
        if (channel != null) {
            parameters.put("channel", channel);
        } else
            parameters.put("channels", true);
        if (after != null) {
            parameters.put("after", after);
        }

        return this.sendRequest("GET", url, parameters, null);
    }

    protected JodelHttpResponse getJodel(String postID) {
        return this.sendRequest("GET", "/v2/posts/" + postID, null);
    }

    protected JodelHttpResponse getJodelV3(String postID, int skip) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("details", "true");
        parameters.put("reply", skip);
        return this.sendRequest("GET", "/v3/posts/" + postID + "/details", parameters, null);
    }

    protected JodelHttpResponse getJodelShareURL(String postID) {
        return this.sendRequest("POST", "/v3/posts/" + postID + "/share", null);
    }

    protected JodelHttpResponse getRecommendedChannels() {
        return this.sendRequest("GET", "/v3/user/recommendedChannels", null);
    }

    protected JodelHttpResponse getChannelMeta(String channel) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("channel", channel);
        return this.sendRequest("GET", "/v3/user/channelMeta", parameters, null);
    }

    protected JodelHttpResponse getKarma() {
//        return this.sendRequest("GET", "/v2/users/karma", null);
        return this.sendRequest("GET", "/v3/user/stats", null);
    }

    protected JodelHttpResponse followChannel(String channel) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("channel", channel);
        return this.sendRequest("PUT", "/v3/user/followChannel", parameters, null);
    }

    protected JodelHttpResponse unfollowChannel(String channel) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("channel", channel);
        return this.sendRequest("PUT", "/v3/user/unfollowChannel", parameters, null);
    }

    protected JodelHttpResponse submitCaptcha(String captchaKey, List<Integer> answer) {
        JsonArray answerArr = new JsonArray();
        for (int answerPosition : answer) {
            answerArr.add(answerPosition);
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("key", captchaKey);
        payload.add("answer", answerArr);
        return this.sendRequest("POST", "/v3/user/verification/imageCaptcha", payload);
    }

    protected JodelHttpResponse submitPost(String message, String base64Image, String color, int ancestor, String channel) {
        JsonObject payload = new JsonObject();
        if (message != null) {
            payload.addProperty("message", message);
        }
        if (base64Image != null) {
            payload.addProperty("image", base64Image);
        }
        if (color != null) {
            payload.addProperty("color", color);
        } else {
            String postColor = JodelHelper.getRandomColor();
            payload.addProperty("color", postColor);
        }
        if (ancestor != 0) {
            payload.addProperty("ancestor", ancestor);
        }
        if (channel != null) {
            payload.addProperty("channel", channel);
        }
        payload.add("location", this.locationObject);
        return this.sendRequest("POST", "/v3/posts/", payload);
    }

    protected JodelHttpResponse performUpvote(String postID, boolean home) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("home", home);
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/upvote", parameters, new JsonObject());
    }

    protected JodelHttpResponse performDownvote(String postID, boolean home) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("home", home);
        return this.sendRequest("PUT", "/v2/posts/" + postID + "/downvote", parameters, new JsonObject());
    }

    protected JodelHttpResponse performUpvoteSticky(String postID) {
        return this.sendRequest("PUT", "/v3/stickyposts/" + postID + "/up", null);
    }

    protected JodelHttpResponse performDownvoteSticky(String postID) {
        return this.sendRequest("PUT", "/v3/stickyposts/" + postID + "/down", null);
    }

    protected JodelHttpResponse performDismissSticky(String postID) {
        return this.sendRequest("PUT", "/v3/stickyposts/" + postID + "/dismiss", null);
    }

    protected JodelHttpResponse performThank(String postID) {
        return this.sendRequest("POST", "/v3/posts/" + postID + "/giveThanks", null);
    }

    private JodelHttpResponse sendRequest(String method, String endpoint, Map<String, Object> params, JsonObject payload) {
        JodelHttpResponse httpResponse = new JodelHttpResponse();
        String fullUrl = JodelAccount.API_URL + endpoint;

        String payloadString = payload != null ? payload.toString() : null;

        HttpsURLConnection con = null;

        try {
            fullUrl = params != null ? fullUrl + "?" + getDataString(params) : fullUrl;

            URL url = new URL(fullUrl);
            if (proxy != null && !proxy.equals(Proxy.NO_PROXY)) {
                con = (HttpsURLConnection) url.openConnection(proxy);
                con.setConnectTimeout(4_000);
                con.setReadTimeout(8_000);
            } else
                con = (HttpsURLConnection) url.openConnection();

            if (!method.equals("GET")) {
                con.setDoOutput(true);
            }
            con.setRequestMethod(method);
            con.setRequestProperty("User-Agent", "Jodel/" + JodelAccount.VERSION + " Dalvik/2.1.0 (Linux; U; Android 6.0; Android SDK built for x86 Build/MASTER)");
            con.setRequestProperty("Accept-Encoding", "gzip");
            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (this.accessToken != null)
                con.setRequestProperty("Authorization", "Bearer " + this.accessToken);

            // perform signing
            Map<String, String> requestSignProperties = getRequestSignProperties(method, url, params, payloadString);
            for (Map.Entry<String, String> entry : requestSignProperties.entrySet()) {
                con.setRequestProperty(entry.getKey(), entry.getValue());
            }


            con.setUseCaches(false);
            con.setDoInput(true);
            con.connect();

            if (!method.equals("GET")) {
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(con.getOutputStream(), StandardCharsets.UTF_8));
                if (payload != null) {
                    pw.write(payloadString);
                }
                pw.flush();
                pw.close();
            }

            int responseCode = con.getResponseCode();
            httpResponse.setStatusCode(responseCode);
            httpResponse.setHeaders(con.getHeaderFields());

            InputStream is;
            if (con.getContentEncoding() != null && con.getContentEncoding().toLowerCase().contains("gzip"))
                is = new GZIPInputStream(responseCode < HttpsURLConnection.HTTP_BAD_REQUEST ? con.getInputStream() : con.getErrorStream());
            else
                is = responseCode < HttpsURLConnection.HTTP_BAD_REQUEST ? con.getInputStream() : con.getErrorStream();

            httpResponse.setContent(readStreamToEnd(is));
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return httpResponse;
    }

    private JodelHttpResponse sendRequest(String method, String endpoint, JsonObject payload) {
        return this.sendRequest(method, endpoint, null, payload);
    }

    private Map<String, String> getRequestSignProperties(String method, URL url, Map<String, Object> params, String payload) {
        Map<String, String> requestSignProperties = new HashMap<String, String>();

        String timestamp = Instant.now().toString().substring(0, 19) + "Z";
        String location = this.latitude.substring(0, this.latitude.length() - 3) + ";" + this.longitude.substring(0, this.longitude.length() - 3);


        List<String> req = new ArrayList<String>();
        req.add(method);
        req.add(url.getAuthority());
        req.add("443");
        req.add(url.getPath());
        if (this.accessToken != null) {
            req.add(this.accessToken);
            req.add(location);
        } else {
            req.add("%");
        }
        req.add(timestamp);

        if (params != null) {
            List<String> paramsList = new ArrayList<String>();
            for (Object key : params.keySet()) {
                String paramString = "";
                //based on you key types
                String keyStr = (String) key;
                Object keyVal = params.get(keyStr);

                paramString += keyStr + "%" + keyVal;
                paramsList.add(paramString);
            }
            req.add(String.join("%", paramsList));
        } else {
            req.add("");
        }

        if (payload != null)
            req.add(payload);
        else
            req.add("");

        String hmacData = String.join("%", req);
        String signature = null;
        try {
            signature = JodelHelper.calculateHMAC(JodelAccount.SECRET, hmacData);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        requestSignProperties.put("X-Authorization", "HMAC " + signature);
        requestSignProperties.put("X-Client-Type", "android_" + JodelAccount.VERSION);
        requestSignProperties.put("X-Timestamp", timestamp);
        requestSignProperties.put("X-Api-Version", "0.2");
        if (this.accessToken != null)
            requestSignProperties.put("X-Location", location);

        return requestSignProperties;
    }

}
