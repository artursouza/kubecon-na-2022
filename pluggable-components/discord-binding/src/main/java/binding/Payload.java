package binding;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for Discord binding.
 * <p>
 * Compatible with: https://github.com/dapr/samples/blob/master/twitter-sentiment-processor/demos/javademo/provider/src/main/java/io/dapr/apps/twitter/provider/model/Tweet.java
 * </p>
 */
public class Payload {
  @JsonProperty("id_str")
  private String id;

  @JsonProperty("user")
  private User author;

  @JsonProperty("full_text")
  private String fullText;

  @JsonProperty("text")
  private String text;

  @JsonProperty("lang")
  private String language;

  public String getId() {
      return id;
  }

  public void setId(String id) {
      this.id = id;
  }

  public User getAuthor() {
      return author;
  }

  public void setAuthor(User author) {
      this.author = author;
  }

  public String getFullText() {
      return fullText;
  }

  public void setFullText(String fullText) {
      this.fullText = fullText;
  }

  public String getText() {
      return text;
  }

  public void setText(String text) {
      this.text = text;
  }

  public String getLanguage() {
      return language;
  }

  public void setLanguage(String language) {
      this.language = language;
  }

  @Override
  public String toString() {
      return "Payload [author=" + author + ", fullText=" + fullText + ", id=" + id + ", language=" + language
              + ", text=" + text + "]";
  }

  public static class User {
    @JsonProperty("name")
    private String name;

    @JsonProperty("screen_name")
    private String screenName;

    @JsonProperty("profile_image_url_https")
    private String picture;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", picture=" + picture + ", screenName=" + screenName + "]";
    }
  }
}
