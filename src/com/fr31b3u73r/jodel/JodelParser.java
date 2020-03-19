package com.fr31b3u73r.jodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class JodelParser {
    /**
     * Parses a list of Jodels
     *
     * @param jodelsJSON String containing a JSON with jodels (from rawResponse)
     * @return A list of objects from type JodelPost
     */
    public static List<JodelPost> getParsedJodels(String jodelsJSON) {
        List<JodelPost> result = new ArrayList<>();
        try {
            JsonObject responsePostJson = JsonParser.parseString(jodelsJSON).getAsJsonObject();
            JsonArray jodelsArray = responsePostJson.getAsJsonArray("posts");

            for (JsonElement jsonElement : jodelsArray) {
                JsonObject jodel = jsonElement.getAsJsonObject();
                JodelPost jodelPost = new JodelPost();
                jodelPost.message = jodel.get("message").getAsString();
                jodelPost.createdAt = jodel.get("created_at").getAsString();
                jodelPost.updatedAt = jodel.get("updated_at").getAsString();
                jodelPost.pinCount = jodel.get("pin_count").getAsLong();
                jodelPost.color = jodel.get("color").getAsString();
                jodelPost.gotThanks = jodel.get("got_thanks").getAsBoolean();
                jodelPost.thanksCount = jodel.get("thanks_count").getAsLong();
                try {
                    jodelPost.imageApproved = jodel.get("image_approved").getAsBoolean();
                    jodelPost.imageURL = jodel.get("image_url").getAsString();
                    jodelPost.thumbnailURL = jodel.get("thumbnail_url").getAsString();
                } catch (Exception ignored) {
                }
                try {
                    jodelPost.fromHome = jodel.get("from_home").getAsBoolean();
                } catch (Exception ignored) {
                }
                try {
                    jodelPost.childCount = jodel.get("child_count").getAsLong();
                } catch (Exception ignored) {
                }
                jodelPost.replier = jodel.get("replier").getAsLong();
                jodelPost.postID = jodel.get("post_id").getAsString();
                jodelPost.discoveredBy = jodel.get("discovered_by").getAsLong();
                jodelPost.voteCount = jodel.get("vote_count").getAsLong();
                jodelPost.shareCount = jodel.get("share_count").getAsLong();
                jodelPost.userHandle = jodel.get("user_handle").getAsString();
                jodelPost.postOwn = jodel.get("post_own").getAsString();
                jodelPost.distance = jodel.get("distance").getAsLong();
                result.add(jodelPost);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Parses a single Jodel (with replies)
     *
     * @param jodelJSON String containing a JSON with a single jodel (from rawResponse)
     * @return An object of type JodelPost containing parsed Jodel with replies
     */
    public static JodelPost getParsedJodel(String jodelJSON) {
        JodelPost jodelPost = new JodelPost();
        try {
            JsonObject responsePostJson = JsonParser.parseString(jodelJSON).getAsJsonObject();

            jodelPost.message = responsePostJson.get("message").getAsString();
            jodelPost.createdAt = responsePostJson.get("created_at").getAsString();
            jodelPost.updatedAt = responsePostJson.get("updated_at").getAsString();
            jodelPost.pinCount = responsePostJson.get("pin_count").getAsLong();
            jodelPost.color = responsePostJson.get("color").getAsString();
            jodelPost.gotThanks = responsePostJson.get("got_thanks").getAsBoolean();
            jodelPost.thanksCount = responsePostJson.get("thanks_count").getAsLong();
            try {
                jodelPost.imageApproved = responsePostJson.get("image_approved").getAsBoolean();
                jodelPost.imageURL = responsePostJson.get("image_url").getAsString();
                jodelPost.thumbnailURL = responsePostJson.get("thumbnail_url").getAsString();
            } catch (Exception ignored) {
            }
            try {
                jodelPost.fromHome = responsePostJson.get("from_home").getAsBoolean();
            } catch (Exception ignored) {
            }
            try {
                jodelPost.childCount = responsePostJson.get("child_count").getAsLong();
            } catch (Exception ignored) {
            }
            jodelPost.replier = responsePostJson.get("replier").getAsLong();
            jodelPost.postID = responsePostJson.get("post_id").getAsString();
            jodelPost.discoveredBy = responsePostJson.get("discovered_by").getAsLong();
            jodelPost.voteCount = responsePostJson.get("vote_count").getAsLong();
            jodelPost.shareCount = responsePostJson.get("share_count").getAsLong();
            jodelPost.userHandle = responsePostJson.get("user_handle").getAsString();
            jodelPost.postOwn = responsePostJson.get("post_own").getAsString();
            jodelPost.distance = responsePostJson.get("distance").getAsLong();

            JsonArray jodelsRepliesArray = responsePostJson.getAsJsonArray("children");

            for (JsonElement jsonElement : jodelsRepliesArray) {
                JsonObject jodelReply = jsonElement.getAsJsonObject();
                JodelPostReply jodelPostReply = new JodelPostReply();
                jodelPostReply.message = jodelReply.get("message").getAsString();
                jodelPostReply.createdAt = jodelReply.get("created_at").getAsString();
                jodelPostReply.updatedAt = jodelReply.get("updated_at").getAsString();
                jodelPostReply.color = jodelReply.get("color").getAsString();
                jodelPostReply.thanksCount = jodelReply.get("thanks_count").getAsLong();
                jodelPostReply.postID = jodelReply.get("post_id").getAsString();
                try {
                    jodelPostReply.imageApproved = jodelReply.get("image_approved").getAsBoolean();
                    jodelPostReply.imageURL = jodelReply.get("image_url").getAsString();
                    jodelPostReply.thumbnailURL = jodelReply.get("thumbnail_url").getAsString();
                } catch (Exception ignored) {
                }
                jodelPostReply.discoveredBy = jodelReply.get("discovered_by").getAsLong();
                jodelPostReply.voteCount = jodelReply.get("vote_count").getAsLong();
                jodelPostReply.userHandle = jodelReply.get("user_handle").getAsString();
                jodelPostReply.postOwn = jodelReply.get("post_own").getAsString();
                jodelPostReply.distance = jodelReply.get("distance").getAsLong();
                jodelPost.replies.add(jodelPostReply);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jodelPost;
    }

    /**
     * Parses a single Jodel (with replies) in V3 of API
     *
     * @param jodelJSON String containing a JSON with a single jodel (from rawResponse)
     * @return An object of type JodelPost containing parsed Jodel with replies
     */
    public static JodelPost getParsedJodelV3(String jodelJSON) {
        JodelPost jodelPost = new JodelPost();
        try {
            JsonObject responsePostJson = JsonParser.parseString(jodelJSON).getAsJsonObject();
            JsonObject postContentJson = responsePostJson.getAsJsonObject("details");

            jodelPost.message = postContentJson.get("message").getAsString();
            jodelPost.createdAt = postContentJson.get("created_at").getAsString();
            jodelPost.updatedAt = postContentJson.get("updated_at").getAsString();
            jodelPost.pinCount = postContentJson.get("pin_count").getAsLong();
            jodelPost.color = postContentJson.get("color").getAsString();
            jodelPost.gotThanks = postContentJson.get("got_thanks").getAsBoolean();
            try {
                jodelPost.imageApproved = postContentJson.get("image_approved").getAsBoolean();
                jodelPost.imageURL = postContentJson.get("image_url").getAsString();
                jodelPost.thumbnailURL = postContentJson.get("thumbnail_url").getAsString();
            } catch (Exception ignored) {
            }
            try {
                jodelPost.fromHome = postContentJson.get("from_home").getAsBoolean();
            } catch (Exception ignored) {
            }
            try {
                jodelPost.childCount = postContentJson.get("child_count").getAsLong();
            } catch (Exception ignored) {
            }
            jodelPost.replier = postContentJson.get("replier").getAsLong();
            jodelPost.postID = postContentJson.get("post_id").getAsString();
            jodelPost.discoveredBy = postContentJson.get("discovered_by").getAsLong();
            jodelPost.voteCount = postContentJson.get("vote_count").getAsLong();
            jodelPost.shareCount = postContentJson.get("share_count").getAsLong();
            jodelPost.userHandle = postContentJson.get("user_handle").getAsString();
            jodelPost.postOwn = postContentJson.get("post_own").getAsString();
            jodelPost.distance = postContentJson.get("distance").getAsLong();

            JsonArray jodelsRepliesArray = responsePostJson.getAsJsonArray("replies");

            for (JsonElement jsonElement : jodelsRepliesArray) {
                JsonObject jodelReply = jsonElement.getAsJsonObject();
                JodelPostReply jodelPostReply = new JodelPostReply();
                jodelPostReply.message = jodelReply.get("message").getAsString();
                jodelPostReply.createdAt = jodelReply.get("created_at").getAsString();
                jodelPostReply.updatedAt = jodelReply.get("updated_at").getAsString();
                jodelPostReply.pinCount = jodelReply.get("pin_count").getAsLong();
                jodelPostReply.color = jodelReply.get("color").getAsString();
                jodelPostReply.gotThanks = jodelReply.get("got_thanks").getAsBoolean();
                jodelPostReply.thanksCount = jodelReply.get("thanks_count").getAsLong();
                jodelPostReply.childCount = jodelReply.get("child_count").getAsLong();
                jodelPostReply.replier = jodelReply.get("replier").getAsLong();
                jodelPostReply.postID = jodelReply.get("post_id").getAsString();
                try {
                    jodelPostReply.imageApproved = jodelReply.get("image_approved").getAsBoolean();
                    jodelPostReply.imageURL = jodelReply.get("image_url").getAsString();
                    jodelPostReply.thumbnailURL = jodelReply.get("thumbnail_url").getAsString();
                } catch (Exception ignored) {
                }
                try {
                    jodelPostReply.fromHome = jodelReply.get("from_home").getAsBoolean();
                } catch (Exception ignored) {
                }
                jodelPostReply.discoveredBy = jodelReply.get("discovered_by").getAsLong();
                jodelPostReply.voteCount = jodelReply.get("vote_count").getAsLong();
                jodelPostReply.userHandle = jodelReply.get("user_handle").getAsString();
                jodelPostReply.postOwn = jodelReply.get("post_own").getAsString();
                jodelPostReply.distance = jodelReply.get("distance").getAsLong();
                jodelPost.replies.add(jodelPostReply);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jodelPost;
    }

    /**
     * Parses a list of Jodel-notifications
     *
     * @param notificationsJSON String containing a JSON with notifications
     * @return A list of objects from type JodelNotification
     */
    public static List<JodelNotification> getParsedNotifications(String notificationsJSON) {
        List<JodelNotification> jodelNotifications = new ArrayList<JodelNotification>();
        try {
            JsonObject responseJson = JsonParser.parseString(notificationsJSON).getAsJsonObject();
            JsonArray jodelsNotificationsArray = responseJson.getAsJsonArray("notifications");

            for (JsonElement jsonElement : jodelsNotificationsArray) {
                JsonObject notification = jsonElement.getAsJsonObject();
                JodelNotification jodelNotification = new JodelNotification();
                jodelNotification.postID = notification.get("post_id").getAsString();
                jodelNotification.type = notification.get("type").getAsString();
                jodelNotification.userID = notification.get("user_id").getAsString();
                jodelNotification.message = notification.get("message").getAsString();
                if (jodelNotification.type.equals("vote_post")) {
                    jodelNotification.voteCount = notification.get("vote_count").getAsLong();
                }
                ;
                jodelNotification.scroll = notification.get("scroll").getAsString();
                jodelNotification.lastInteraction = notification.get("last_interaction").getAsString();
                jodelNotification.read = notification.get("read").getAsBoolean();
                jodelNotification.seen = notification.get("seen").getAsBoolean();
                jodelNotification.color = notification.get("color").getAsString();
                jodelNotification.notificationID = notification.get("notification_id").getAsString();

                jodelNotifications.add(jodelNotification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jodelNotifications;
    }
}
