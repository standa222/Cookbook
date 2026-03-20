# Project Description - Cookbook

## 1. Application Description
The main purpose of the application is to help users manage and organize their personal recipes. This includes both recording the ingredients (with desired quantity) and a step-by-step description of the whole cooking process. The application operates primarily offline using a local database, ensuring that cooking instructions are always accessible in the kitchen, regardless of internet connectivity.

### Target Users
* **Home Cooks:** Individuals looking to digitalize their family recipes.
* **Meal Planners:** Users who need to filter recipes or search for specific ingredients.
* **Beginners:** Less experienced cooks who feel safer following a structured step-by-step guide.

---

## 2. Basic Entities
The database utilizes a relational structure to ensure data integrity and support complex queries like filtering and searching.

### **Recipe** - core entity
* `id`: Long (Primary Key)
* `title`: String
* `description`: String
* `prepTimeMinutes`: Int
* `categoryId`: Long (Foreign Key)
* `isFavorite`: Boolean

### **Category**
* `id`: Long (Primary Key)
* `name`: String (e.g., Savory, Sweet, Baking)

### **Ingredient**
* `id`: Long (Primary Key)
* `recipeId`: Long (Foreign Key)
* `name`: String
* `amount`: String (e.g., "200g", "1 tsp")

### **InstructionStep**
* `id`: Long (Primary Key)
* `recipeId`: Long (Foreign Key)
* `stepNumber`: Int
* `description`: String
* `timerDurationMinutes`: Int (Optional for notifications)

---

## 3. REST API
For the network communication phase (Part A4), the application will interface with a standard REST API designed for recipe synchronization. The main endpoint allows the application to fetch or backup recipes to a remote server.

### Endpoints:
* `GET /recipe` - get all recipes
* `GET /recipe/{id}` - get recipe detail (ingredients and guide)
* `POST /recipe` - Upload a new local recipe to the server.
* `PUT /recipe/{id}` -  Update an existing recipe record.
* `DELETE /recipe/{id}` - Remove a recipe from the remote storage.

### **JSON Schema Example**
```json
{
  "id": 101,
  "title": "Homemade Pizza",
  "description": "A classic thin-crust Italian pizza.",
  "prepTimeMinutes": 45,
  "category": "Savory",
  "isFavorite": false,
  "ingredients": [
    {
      "name": "Flour",
      "amount": "500g"
    },
    {
      "name": "Dry Yeast",
      "amount": "7g"
    }
  ],
  "instructions": [
    {
      "stepNumber": 1,
      "description": "Mix flour, yeast, and water in a large bowl.",
      "timerDurationMinutes": 10
    },
    {
      "stepNumber": 2,
      "description": "Knead the dough until smooth.",
      "timerDurationMinutes": null
    }
  ]
}
