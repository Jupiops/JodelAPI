package com.fr31b3u73r.jodel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class JodelAccount {

    static final String API_URL = "https://api.go-tellm.com/api";
    static final String CLIENT_ID = "81e8a76e-1e02-4d17-9ba0-8a7020261b26";
    static final String SECRET = "TNHfHCaBjTvtrjEFsAFQyrHapTHdKbJVcraxnTzd";
    static final String VERSION = "5.16.1";
    JsonObject locationObject = new JsonObject();
    String accessToken = null;
    String deviceUID = null;
    String expirationDate = null;
    String distinctID = null;
    String refreshToken = null;
    String latitude = null;
    String longitude = null;
    double locationAccuracy = 0.0;
    Locale country;
    JodelHttpAction httpAction;
    Proxy proxy;

    /**
     * Constructor for JodelAccount class
     *
     * @param lat               Latitute of location
     * @param lng               Longitude of location
     * @param city              Name of the city
     * @param country           Countrycode (e.g. DE for Germany)
     * @param name              Name of the location (normally the same as city)
     * @param updateLocation    Boolean to update a location for existing account
     * @param randomizeLocation Boolean to randomize the Location
     * @param accessToken       Access token of account
     * @param deviceUID         Device ID of account
     * @param refreshToken      Refresh token of account
     * @param distinctID        Distinct ID of account
     * @param expirationDate    Timestamp of expiration date
     * @param proxy             Proxy for connections to the jodel servers
     */
    public JodelAccount(String lat, String lng, String city, Locale country, String name, Boolean updateLocation, Boolean randomizeLocation,
                        String accessToken, String deviceUID, String refreshToken, String distinctID, String expirationDate, Proxy proxy) {

        this.httpAction = new JodelHttpAction();
        this.proxy = proxy;

        this.latitude = randomizeLocation ? JodelHelper.randomizeGeography(lat) : lat;
        this.longitude = randomizeLocation ? JodelHelper.randomizeGeography(lng) : lng;
        this.locationAccuracy = randomizeLocation ? (Math.round(ThreadLocalRandom.current().nextDouble(16) * 1000d) / 1000d) : this.locationAccuracy;
        this.country = country;

        this.locationObject.addProperty("city", city);
        this.locationObject.addProperty("loc_accuracy", this.locationAccuracy);
        this.locationObject.addProperty("country", this.country.getCountry());
//        this.locationObject.put("name", name);
        JsonObject locationCoordinates = new JsonObject();
        locationCoordinates.addProperty("lat", this.latitude);
        locationCoordinates.addProperty("lng", this.longitude);
        this.locationObject.add("loc_coordinates", locationCoordinates);

        this.deviceUID = deviceUID;

        if (accessToken != null && refreshToken != null && distinctID != null && expirationDate != null) {
            this.expirationDate = expirationDate;
            this.distinctID = distinctID;
            this.refreshToken = refreshToken;


            this.accessToken = accessToken;

            if (updateLocation) {
                this.updateHTTPParameter();
                this.httpAction.setLocation();
            }
        } else {
            this.refreshAllTokens();
        }

    }

    /**
     * Overloads constructor using JodelAccountData object instead of single values for account data
     *
     * @param lat               Latitute of location
     * @param lng               Longitude of location
     * @param city              Name of the city
     * @param country           Countrycode (e.g. DE for Germany)
     * @param name              Name of the location (normally the same as city)
     * @param updateLocation    Boolean to update a location for existing account
     * @param randomizeLocation Boolean to randomize the Location
     * @param accountData       Object of type JodelAccountData containing all necessary account data
     * @param proxy             Proxy for connections to the jodel servers
     */
    public JodelAccount(String lat, String lng, String city, Locale country, String name, Boolean updateLocation, Boolean randomizeLocation, JodelAccountData accountData, Proxy proxy) {
        this(lat, lng, city, country, name, updateLocation, randomizeLocation, accountData.accessToken, accountData.deviceUID, accountData.refreshToken, accountData.distinctID, accountData.expirationDate, proxy);
    }

    /**
     * Overloads constructor to simplify creating a new account
     *
     * @param lat               Latitute of location
     * @param lng               Longitude of location
     * @param city              Name of the city
     * @param country           Countrycode (e.g. DE for Germany)
     * @param name              Name of the location (normally the same as city)
     * @param randomizeLocation Boolean to randomize the Location
     * @param proxy             Proxy for connections to the jodel servers
     */
    public JodelAccount(String lat, String lng, String city, Locale country, String name, Boolean randomizeLocation, Proxy proxy) {
        this(lat, lng, city, country, name, false, randomizeLocation, null, null, null, null, null, proxy);
    }

    /**
     * Returns current account data as object of type JodelAccountData
     *
     * @return Account data
     */
    public JodelAccountData getAccountData() {
        JodelAccountData myJodelAccountData = new JodelAccountData();
        myJodelAccountData.accessToken = this.accessToken;
        myJodelAccountData.deviceUID = this.deviceUID;
        myJodelAccountData.distinctID = this.distinctID;
        myJodelAccountData.expirationDate = this.expirationDate;
        myJodelAccountData.refreshToken = this.refreshToken;
        return myJodelAccountData;
    }


    /**
     * Creates a new account with random ID if self.device_uid is not set. Otherwise renews all tokens of the account with ID = this.device_uid.
     */
    public void refreshAllTokens() {
        if (this.deviceUID == null) {
            char[] validChars = "abcdef0123456789".toCharArray();
            StringBuilder sb = new StringBuilder();
            Random random = new Random();
            for (int i = 0; i < 64; i++) {
                char c = validChars[random.nextInt(validChars.length)];
                sb.append(c);
            }
            this.deviceUID = sb.toString();
        }

        this.updateHTTPParameter();
        JodelHttpResponse requestResponse = this.httpAction.getNewTokens();

        if (requestResponse.getStatusCode() == 200) {
            String responseMessage = requestResponse.toString();

            JsonObject responseJson = JsonParser.parseString(responseMessage).getAsJsonObject();
            this.accessToken = responseJson.get("access_token").getAsString();
            this.expirationDate = responseJson.get("expiration_date").getAsString();
            this.refreshToken = responseJson.get("refresh_token").getAsString();
            this.distinctID = responseJson.get("distinct_id").getAsString();
        } else {
            System.err.println("getNewTokens faild! HTTP Status Code: " + requestResponse.getStatusCode()
                    + " for Device UID: " + this.deviceUID + "\n" + requestResponse);
        }
    }

    /**
     * Refreshes the Access Token of the currently used account
     */
    public void refreshAccessToken() {
        this.updateHTTPParameter();
        JodelHttpResponse requestResponse = this.httpAction.getNewAccessToken();
        if (requestResponse.getStatusCode() == 200) {
            JsonObject responseJson = JsonParser.parseString(requestResponse.toString()).getAsJsonObject();
            this.accessToken = responseJson.get("access_token").toString();
            this.expirationDate = responseJson.get("expiration_date").toString();
        }
    }


    public JodelRequestResponse sendPushToken(String pushToken) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse requestUserResponse = this.httpAction.getUserConfig();

        if (requestUserResponse.getStatusCode() == 200) {
            String responseUserMessage = requestUserResponse.toString();
            try {
                JsonObject responseJson = JsonParser.parseString(responseUserMessage).getAsJsonObject();
                boolean verrifiedStatus = responseJson.get("verified").getAsBoolean();
                if (!verrifiedStatus) {
                    System.out.println("Not verified jet!");
                    this.updateHTTPParameter();
                    JodelHttpResponse requestPushTokenResponse = this.httpAction.sendPushToken(pushToken);
                    requestResponse.httpResponseCode = requestPushTokenResponse.getStatusCode();
                    if (requestPushTokenResponse.getStatusCode() == 200) {
                        requestResponse.rawResponseMessage = requestPushTokenResponse.toString();
                    }
                }
            } catch (Exception e) {
                requestResponse.rawErrorMessage = e.getMessage();
                e.printStackTrace();
                requestResponse.error = true;
                requestResponse.errorMessage = "Could not parse response JSON!";
            }
        } else {
            requestResponse.error = true;
            requestResponse.rawErrorMessage = requestUserResponse.toString();
            requestResponse.errorMessage = "Response Code = " + requestUserResponse.getStatusCode();
        }
        return requestResponse;
    }

    public JodelRequestResponse verifyPush(String verificationCode, long serverTime) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse submitCaptchaVerification = this.httpAction.verifyPush(verificationCode, serverTime);
        requestResponse.httpResponseCode = submitCaptchaVerification.getStatusCode();
        if (submitCaptchaVerification.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = submitCaptchaVerification.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets the captcha image url and key for not verified users
     *
     * @return Object of type JodelRequestResponse containing captcha URL and key in responseValues attribute
     */
    public JodelRequestResponse getCaptchaData() {
        JodelRequestResponse requestResponse = new JodelRequestResponse();

        this.updateHTTPParameter();
        JodelHttpResponse requestUserResponse = this.httpAction.getUserConfig();
        if (requestUserResponse.getStatusCode() == 200) {
            String responseUserMessage = requestUserResponse.toString();
            try {
                JsonObject responseJson = JsonParser.parseString(responseUserMessage).getAsJsonObject();
                if (!responseJson.get("verified").getAsBoolean()) {
                    this.updateHTTPParameter();
                    JodelHttpResponse requestCaptchaResponse = this.httpAction.getCaptcha();
                    requestResponse.httpResponseCode = requestCaptchaResponse.getStatusCode();
                    if (requestCaptchaResponse.getStatusCode() == 200) {
                        String responseCaptchaMessage = requestCaptchaResponse.toString();
                        requestResponse.rawResponseMessage = responseCaptchaMessage;
                        try {
                            JsonObject responseCaptchaJson = JsonParser.parseString(responseCaptchaMessage).getAsJsonObject();
                            String captchaUrl = responseCaptchaJson.get("image_url").getAsString();
                            String captchaKey = responseCaptchaJson.get("key").getAsString();
                            requestResponse.responseValues.put("captchaUrl", captchaUrl);
                            requestResponse.responseValues.put("captchaKey", captchaKey);

                        } catch (Exception e) {
                            requestResponse.rawErrorMessage = e.getMessage();
                            e.printStackTrace();
                            requestResponse.error = true;
                            requestResponse.errorMessage = "Could not parse response JSON!";
                        }
                    }
                }
            } catch (Exception e) {
                requestResponse.rawErrorMessage = e.getMessage();
                e.printStackTrace();
                requestResponse.error = true;
                requestResponse.errorMessage = "Could not parse response JSON!";
            }
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Solves the captcha so that a user can verify his account
     *
     * @param key       Key of captcha
     * @param positions List of positions with racoon (starting with 0 from left to right)
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse verifyCaptcha(String key, List<Integer> positions) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse submitCaptchaVerification = this.httpAction.submitCaptcha(key, positions);
        requestResponse.httpResponseCode = submitCaptchaVerification.getStatusCode();
        if (submitCaptchaVerification.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = submitCaptchaVerification.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Creates a new Jodel
     *
     * @param message     Text message for new Jodel
     * @param base64Image Base64 encoded image
     * @param color       Color of the Jodel
     * @param channel     Channel to post Jodel to
     * @param ancestor    For replies specify an ancestor
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse createPost(String message, String base64Image, String color, String channel, int ancestor) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        if (message == null && base64Image == null) {
            requestResponse.error = true;
            requestResponse.errorMessage = "Message or Image is mandatory!";
            return requestResponse;
        }
        this.updateHTTPParameter();
        JodelHttpResponse submitPost = this.httpAction.submitPost(message, base64Image, color, ancestor, channel);
        requestResponse.httpResponseCode = submitPost.getStatusCode();
        if (submitPost.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = submitPost.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Creates a new Jodel with no ancestor (i.e. no reply)
     *
     * @param message     Text message for new Jodel
     * @param base64Image Base64 encoded image
     * @param color       Color of the Jodel
     * @param channel     Channel to post Jodel to
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse createPost(String message, String base64Image, String color, String channel) {
        return this.createPost(message, base64Image, color, channel, 0);
    }

    /**
     * Gets Jodels matching given criteria
     *
     * @param postTypes Types of Jodels to return (e.g. popular, discussed etc.)
     * @param page      Pagination
     * @param distance
     * @param after     Return only Jodels after a certain post
     * @param mine      Boolean to set output to only own Jodels
     * @param hashtag   Hashtag to filter Jodels
     * @param channel   Channel to filter Jodels
     * @param pictures  Boolean for selecting pictures
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPosts(String postTypes, int page, String distance, String after, boolean mine, boolean home, String hashtag, String channel, boolean pictures) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();

        String category = "location";
        String apiVersion = "v2";
        String picturesPosts = "posts";
        if (mine) {
            category = "mine";
            postTypes = "combo";
        } else if (hashtag != null) {
            category = "hashtag";
            apiVersion = "v3";
        } else if (channel != null) {
            category = "channel";
            apiVersion = "v3";
        }
        if (pictures) {
            apiVersion = "v3";
            picturesPosts = "pictures";
        }
        if (postTypes == null) {
            postTypes = "";
        }
        String url = "/" + apiVersion + "/" + picturesPosts + "/" + category + "/" + postTypes;

        this.updateHTTPParameter();
        JodelHttpResponse getJodels = this.httpAction.getJodels(url, page, distance, after, hashtag, channel, home, false);
        requestResponse.httpResponseCode = getJodels.getStatusCode();
        if (getJodels.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = getJodels.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets recent Jodels matching given criteria
     *
     * @param page    Pagination
     * @param after   Return only Jodels after a certain post
     * @param mine    Boolean to set output to only own Jodels
     * @param hashtag Hashtag to filter Jodels
     * @param channel Channel to filter Jodels
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPostsRecent(int page, String after, boolean mine, boolean home, String hashtag, String channel) {
        return this.getPosts("", page, null, after, mine, home, hashtag, channel, false);
    }

    /**
     * Gets popular Jodels matching given criteria
     *
     * @param page    Pagination
     * @param after   Return only Jodels after a certain post
     * @param mine    Boolean to set output to only own Jodels
     * @param hashtag Hashtag to filter Jodels
     * @param channel Channel to filter Jodels
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPostsPopular(int page, String after, boolean mine, boolean home, String hashtag, String channel) {
        return this.getPosts("popular", page, null, after, mine, home, hashtag, channel, false);
    }

    /**
     * Gets most discussed Jodels matching given criteria
     *
     * @param page    Pagination
     * @param after   Return only Jodels after a certain post
     * @param mine    Boolean to set output to only own Jodels
     * @param hashtag Hashtag to filter Jodels
     * @param channel Channel to filter Jodels
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPostsDiscussed(int page, String after, boolean mine, boolean home, String hashtag, String channel) {
        return this.getPosts("discussed", page, null, after, mine, home, hashtag, channel, false);
    }

    /**
     * Gets recent picture Jodels matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPicturesRecent(int skip, boolean home, String after) {
        return this.getPosts("", skip, null, after, false, home, null, null, true);
    }

    /**
     * Gets popular picture Jodels matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPicturesPopular(int skip, boolean home, String after) {
        return this.getPosts("popular", skip, null, after, false, home, null, null, true);
    }

    /**
     * Gets most discussed picture Jodels matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPicturesDiscussed(int skip, boolean home, String after) {
        return this.getPosts("discussed", skip, null, after, false, home, null, null, true);
    }

    /**
     * Gets your pinned Jodels matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getMyPinnedPosts(int skip, boolean home, String after) {
        return this.getPosts("pinned", skip, null, after, true, home, null, null, false);
    }

    /**
     * Gets Jodels you replied to matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getMyRepliedPosts(int skip, boolean home, String after) {
        return this.getPosts("replies", skip, null, after, true, home, null, null, false);
    }

    /**
     * Gets Jodels you voted to matching given criteria
     *
     * @param skip  Skip value
     * @param after Return only Jodels after a certain post
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getMyVotedPosts(int skip, boolean home, String after) {
        return this.getPosts("votes", skip, null, after, true, home, null, null, false);
    }

    /**
     * Gets a single Jodel with all replies by post ID
     *
     * @param postID ID of the post to retrieve details for
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPostDetails(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse getJodel = this.httpAction.getJodel(postID);
        requestResponse.httpResponseCode = getJodel.getStatusCode();
        if (getJodel.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = getJodel.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets a single Jodel in new V3 of endpoint with all replies by post ID
     *
     * @param postID ID of the post to retrieve details for
     * @param skip   Skip value
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getPostDetailsV3(String postID, int skip) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse getJodel = this.httpAction.getJodelV3(postID, skip);
        requestResponse.httpResponseCode = getJodel.getStatusCode();
        if (getJodel.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = getJodel.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Upvotes a post
     *
     * @param postID ID of the post to vote
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse upvoteJodel(String postID, boolean home) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse performUpvote = this.httpAction.performUpvote(postID, home);
        requestResponse.httpResponseCode = performUpvote.getStatusCode();
        if (performUpvote.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = performUpvote.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Downvotes a post
     *
     * @param postID ID of the post to vote
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse downvoteJodel(String postID, boolean home) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse performDownvote = this.httpAction.performDownvote(postID, home);
        requestResponse.httpResponseCode = performDownvote.getStatusCode();
        if (performDownvote.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = performDownvote.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Thanks a post
     *
     * @param postID ID of the post to thank
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse thankJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse performThank = this.httpAction.performThank(postID);
        requestResponse.httpResponseCode = performThank.getStatusCode();
        if (performThank.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = performThank.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Get share url
     *
     * @param postID ID of the post to share
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getJodelShareLink(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse getShareLink = this.httpAction.getJodelShareURL(postID);
        requestResponse.httpResponseCode = getShareLink.getStatusCode();
        if (getShareLink.getStatusCode() == 200) {
            String responseJodelsMessage = getShareLink.toString();
            requestResponse.rawResponseMessage = responseJodelsMessage;
            try {
                JsonObject responseJson = JsonParser.parseString(responseJodelsMessage).getAsJsonObject();
                String url = responseJson.get("url").getAsString();
                requestResponse.responseValues.put("shareLink", url);
            } catch (Exception e) {
                requestResponse.rawErrorMessage = e.getMessage();
                e.printStackTrace();
                requestResponse.error = true;
                requestResponse.errorMessage = "Could not parse response JSON!";
            }
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Pins a Jodel
     *
     * @param postID ID of the post to pin
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse pinJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse setPin = this.httpAction.setPin(postID);
        requestResponse.httpResponseCode = setPin.getStatusCode();
        if (setPin.getStatusCode() == 200) {
            requestResponse.rawResponseMessage = setPin.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Unpins a Jodel
     *
     * @param postID ID of the post to unpin
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse unpinJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse removePin = this.httpAction.removePin(postID);
        requestResponse.httpResponseCode = removePin.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = removePin.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Enable notifications of a Jodel
     *
     * @param postID ID of the post to enable notification
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse enableNotification(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse notifyJodel = this.httpAction.enableNotify(postID);
        requestResponse.httpResponseCode = notifyJodel.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = notifyJodel.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Disables notifications of a Jodel
     *
     * @param postID ID of the post to disable notification
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse disableNotification(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse noNotifyJodel = this.httpAction.disableNotify(postID);
        requestResponse.httpResponseCode = noNotifyJodel.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = noNotifyJodel.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Deletes a (own) Jodel
     *
     * @param postID ID of the post to delete
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse deleteJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse removeJodel = this.httpAction.removeJodel(postID);
        requestResponse.httpResponseCode = removeJodel.getStatusCode();
        if (requestResponse.httpResponseCode == 204) {
            requestResponse.rawResponseMessage = removeJodel.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Upvotes a sticky Jodel
     *
     * @param postID ID of the sticky post to upvote
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse upvoteStickyJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse upvoteSticky = this.httpAction.performUpvoteSticky(postID);
        requestResponse.httpResponseCode = upvoteSticky.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = upvoteSticky.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Downvotes a sticky Jodel
     *
     * @param postID ID of the sticky post to downvote
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse downvoteStickyJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse downvoteSticky = this.httpAction.performDownvoteSticky(postID);
        requestResponse.httpResponseCode = downvoteSticky.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = downvoteSticky.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Dismisses a sticky Jodel
     *
     * @param postID ID of the sticky post to dismiss
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse dismissStickyJodel(String postID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse dismissSticky = this.httpAction.performDismissSticky(postID);
        requestResponse.httpResponseCode = dismissSticky.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = dismissSticky.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets Notifications for current account
     *
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getNotifications() {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse getNotify = this.httpAction.getNotifications();
        requestResponse.httpResponseCode = getNotify.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = getNotify.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets new Notifications for current account
     *
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getNotificationsNew() {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse getNotify = this.httpAction.getNotificationsNew();
        requestResponse.httpResponseCode = getNotify.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = getNotify.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Sets a notification "read" by postID or notificationID
     *
     * @param postID         ID of the post to set notification read
     * @param notificationID ID of the notification to set read
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse setNotificationRead(String postID, String notificationID) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse setNotificationRead = null;
        if (postID != null) {
            setNotificationRead = this.httpAction.setNotificationReadPostID(postID);
        } else if (notificationID != null) {
            setNotificationRead = this.httpAction.setNotificationReadNotificationID(notificationID);
        } else {
            requestResponse.error = true;
        }
        requestResponse.httpResponseCode = setNotificationRead.getStatusCode();
        if (requestResponse.httpResponseCode != 204) {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets the recommended channels
     *
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getRecommendedChannels() {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse recommendedChannels = this.httpAction.getRecommendedChannels();
        requestResponse.httpResponseCode = recommendedChannels.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = recommendedChannels.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Gets metadata of a channel
     *
     * @param channel Name of the channel
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getChannelMeta(String channel) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse channelMeta = this.httpAction.getChannelMeta(channel);
        requestResponse.httpResponseCode = channelMeta.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            requestResponse.rawResponseMessage = channelMeta.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Follows a channel
     *
     * @param channel Name of the channel
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse followChannel(String channel) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse follow = this.httpAction.followChannel(channel);
        requestResponse.httpResponseCode = follow.getStatusCode();
        if (requestResponse.httpResponseCode == 204) {
            requestResponse.rawResponseMessage = follow.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Unfollows a channel
     *
     * @param channel Name of the channel
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse unfollowChannel(String channel) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse unfollow = this.httpAction.unfollowChannel(channel);
        requestResponse.httpResponseCode = unfollow.getStatusCode();
        if (requestResponse.httpResponseCode == 204) {
            requestResponse.rawResponseMessage = unfollow.toString();
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    /**
     * Sets the user profile
     *
     * @param userType Type of user (as defined in JodelUsertype)
     * @param gender   Gender (m or f)
     * @param age      Age of the user
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse setUserProfile(String userType, String gender, int age) {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        if (userType != null && gender != null) {
            this.updateHTTPParameter();
            JodelHttpResponse setUserProfile = this.httpAction.setUserProfile(userType, gender, age);
            requestResponse.httpResponseCode = setUserProfile.getStatusCode();
            if (requestResponse.httpResponseCode != 204) {
                requestResponse.error = true;
            }
        } else {
            requestResponse.error = true;
            requestResponse.errorMessage = "Please specify all necessary parameters!";
        }
        return requestResponse;
    }

    /**
     * Gets your karma
     *
     * @return The requestResponse of type JodelRequestResponse
     */
    public JodelRequestResponse getKarma() {
        JodelRequestResponse requestResponse = new JodelRequestResponse();
        this.updateHTTPParameter();
        JodelHttpResponse karmaResponse = this.httpAction.getKarma();
        requestResponse.httpResponseCode = karmaResponse.getStatusCode();
        if (requestResponse.httpResponseCode == 200) {
            String responseKarma = karmaResponse.toString();
            requestResponse.rawResponseMessage = responseKarma;
            try {
                JsonObject responseCaptchaJson = (JsonObject) JsonParser.parseString(responseKarma);
                String karma = responseCaptchaJson.get("karma").toString();
                requestResponse.responseValues.put("karma", karma);
            } catch (Exception e) {
                requestResponse.rawErrorMessage = e.getMessage();
                e.printStackTrace();
                requestResponse.error = true;
                requestResponse.errorMessage = "Could not parse response JSON!";
            }
        } else {
            requestResponse.error = true;
        }
        return requestResponse;
    }

    private void updateHTTPParameter() {
        this.httpAction.updateAccessValues(this.proxy, this.locationObject, this.accessToken, this.distinctID, this.refreshToken, this.deviceUID, this.expirationDate, this.latitude, this.longitude, this.country);
    }
}
