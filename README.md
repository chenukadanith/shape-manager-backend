# Shape Management System

A comprehensive Spring Boot application for managing geometric shapes with overlap detection capabilities. The system supports various shape types including circles, triangles, rectangles, and polygons, with a robust authentication system and RESTful API.

## 🚀 Features

- **Multi-Shape Support**: Circles, triangles, rectangles, and polygons
- **Overlap Detection**: Advanced algorithm to detect overlapping shapes
- **RESTful API**: Complete CRUD operations for shape management
- **Authentication & Authorization**: JWT-based security with role-based access
- **Database Integration**: MySQL with JPA/Hibernate
- **Input Validation**: Comprehensive validation for shape coordinates and properties
- **CORS Support**: Configured for frontend integration

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.5.3, Java 17
- **Database**: MySQL 8.0+
- **Security**: Spring Security with JWT
- **Build Tool**: Maven
- **Testing**: JUnit 5, Spring Boot Test
- **Lombok**: For reducing boilerplate code

## 📋 Prerequisites

Before running this application, ensure you have the following installed:

- **Java 17** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git** (for cloning the repository)

## 🗄️ Database Setup

### 1. Create MySQL Database

```sql
CREATE DATABASE shape_manager_db;
CREATE USER 'shape_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON shape_manager_db.* TO 'shape_user'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure Database Connection

Update the database configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shape_manager_db
spring.datasource.username=shape_user
spring.datasource.password=your_password
```

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ShapeManegement
```

### 2. Build the Application

```bash
mvn clean install
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Verify Installation

Access the application health check:
```bash
curl http://localhost:8080/api/shapes
```


### Authentication Flow

1. **Login**: POST `/api/auth/login`
2. **Get JWT Token**: Use the returned token in subsequent requests
3. **API Access**: Include token in Authorization header: `Bearer <token>`




## 📡 API Endpoints

### Shape Management

| Method | Endpoint | Description | Authentication Required |
|--------|----------|-------------|------------------------|
| GET | `/api/shapes` | Get all shapes | Yes |
| GET | `/api/shapes/{id}` | Get shape by ID | No |
| POST | `/api/shapes` | Create new shape | Yes |
| PUT | `/api/shapes/{id}` | Update shape | Yes |
| DELETE | `/api/shapes/{id}` | Delete shape | Yes |
| GET | `/api/shapes/overlaps` | Get overlapping shape IDs | Yes |

### Request/Response Examples

#### Create a Circle
```json
POST /api/shapes
{
  "name": "My Circle",
  "type": "circle",
  "centerX": 100.0,
  "centerY": 100.0,
  "radius": 50.0
}
```

#### Create a Triangle
```json
POST /api/shapes
{
  "name": "My Triangle",
  "type": "triangle",
  "coordinates": "100,100;200,100;150,200"
}
```

#### Create a Rectangle
```json
POST /api/shapes
{
  "name": "My Rectangle",
  "type": "rectangle",
  "coordinates": "100,100;300,100;300,200;100,200"
}
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Categories

```bash
# Unit tests only
mvn test -Dtest=*Test

# Integration tests only
mvn test -Dtest=*IntegrationTest
```



## 🏗️ Design Decisions & Architecture

### 1. Polymorphic Shape Storage

**Decision**: Single table inheritance with JSON coordinates storage

**Rationale**:
- **Simplicity**: Single table approach reduces complexity
- **Flexibility**: JSON storage allows for different coordinate formats
- **Performance**: Avoids complex joins across multiple tables
- **Scalability**: Easy to add new shape types without schema changes



### 2. Overlap Detection Algorithm

**Algorithm Design**:
1. **Bounding Box Optimization**: Quick pre-filter using bounding boxes
2. **Type-Specific Detection**: Specialized algorithms for different shape combinations
3. **Geometric Calculations**: Mathematical formulas for precise overlap detection

**Performance Optimizations**:
- **Early Exit**: Bounding box check eliminates non-overlapping pairs
- **Efficient Data Structures**: Point objects and optimized coordinate parsing
- **Algorithmic Complexity**: O(n²) with early termination for non-overlapping shapes

**Supported Overlap Types**:
- Circle-Circle: Distance-based calculation
- Circle-Polygon: Point-in-polygon and distance-to-edge checks
- Polygon-Polygon: Line segment intersection and point containment

### 3. Security Architecture

**JWT-Based Authentication**:
- **Stateless**: No server-side session storage
- **Scalable**: Supports distributed deployment
- **Secure**: Token-based with expiration

**Authorization Strategy**:
- **Public Endpoints**: Shape retrieval by ID
- **Protected Endpoints**: All CRUD operations require authentication
- **Role-Based Access**: Admin role for all operations

### 4. Data Validation Strategy

**Multi-Level Validation**:
1. **Input Validation**: Bean validation annotations
2. **Business Logic Validation**: Custom validation in service layer
3. **Coordinate Format Validation**: Regex patterns for coordinate strings
4. **Geometric Validation**: Mathematical validation of shape properties

## ⚠️ Assumptions 

### Assumptions

1. **Coordinate System**: 2D Cartesian coordinate system (X,Y)
2. **Shape Types**: Limited to circles, triangles, rectangles, and polygons
3. **Coordinate Format**: 
   - Polygons: "x1,y1;x2,y2;x3,y3;..."
   - Circles: centerX, centerY, radius
4. **Database**: MySQL with JSON column support
5. **Frontend**: React application running on localhost:5173








