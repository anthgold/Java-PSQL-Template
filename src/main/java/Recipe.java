import java.util.List;
import java.util.ArrayList;
import org.sql2o.*;
import java.util.Date;

public class Recipe {
  private int id;
  private String name;
  private int tags_id;
  private int rating;
  private String instructions;

  public Recipe(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getId() {
    return id;
  }

  public static List<Recipe> all() {
    String sql = "SELECT * FROM recipes";
    try(Connection con = DB.sql2o.open()) {
      return con.createQuery(sql).executeAndFetch(Recipe.class);
    }
  }

  @Override
  public boolean equals(Object otherRecipe){
    if (!(otherRecipe instanceof Recipe)) {
      return false;
    } else {
      Recipe newRecipe = (Recipe) otherRecipe;
      return this.getId() == newRecipe.getId();
    }
  }

  public void save() {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO recipes (name) VALUES (:name)";
      this.id = (int) con.createQuery(sql, true)
      .addParameter("name", this.name)
      .executeUpdate()
      .getKey();
    }
  }

  public static Recipe find(int id) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT * FROM recipes WHERE id=:id";
      Recipe recipe = con.createQuery(sql)
        .addParameter("id", id)
        .executeAndFetchFirst(Recipe.class);
      return recipe;
    }
  }

  public static List<Recipe> findByName(String name) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT * FROM recipes WHERE name=:name";
      List<Recipe> recipes = con.createQuery(sql)
        .addParameter("name", name)
        .executeAndFetch(Recipe.class);
      return recipes;
    }
  }

  public static List<Recipe> findByTag(Tag tag) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT recipes.* FROM tags JOIN recipes_tags ON (tags.id = recipes_tags.tags_id) JOIN recipes ON (recipes_tags.recipes_id = recipes.id) WHERE tags.id = :tags_id";
      List<Recipe> recipes = con.createQuery(sql)
        .addParameter("tags_id", tag.getId())
        .executeAndFetch(Recipe.class);
      return recipes;
    }
  }

  public void addTag(Tag tag) {
    try (Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO recipes_tags (tags_id, recipes_id) VALUES (:tags_id, :recipes_id);";
      con.createQuery(sql, true)
        .addParameter("tags_id", tag.getId())
        .addParameter("recipes_id", this.getId())
        .executeUpdate()
        .getKey();
    }
  }

  public List<Tag> getTags() {
    try(Connection con = DB.sql2o.open()) {
      String joinQuery = "SELECT tags_id FROM recipes_tags WHERE recipes_id = :recipes_id;";

      List<Integer> tagIds = con.createQuery(joinQuery)
        .addParameter("recipes_id", this.getId())
        .executeAndFetch(Integer.class);

      List<Tag> tags = new ArrayList<Tag>();

      for (Integer tagId : tagIds) {
        String recipeQuery = "SELECT * FROM tags WHERE id = :tagId;";
        Tag tag = con.createQuery(recipeQuery)
          .addParameter("tagId", tagId)
          .executeAndFetchFirst(Tag.class);
        tags.add(tag);
      }
      return tags;
    }
  }

  public void delete() {
    try(Connection con = DB.sql2o.open()) {
      String deleteQuery = "DELETE FROM recipes WHERE id = :id;";
        con.createQuery(deleteQuery)
          .addParameter("id", this.getId())
          .executeUpdate();

      String joinDeleteQuery = "DELETE FROM recipes_tags WHERE recipes_id = :recipeId;";
        con.createQuery(joinDeleteQuery)
          .addParameter("recipeId", this.getId())
          .executeUpdate();
    }
  }

}
