# E-commerce Application with JWT Authentication

A modern Spring Boot e-commerce application featuring comprehensive JWT authentication, customer management, product catalog, and order processing capabilities.

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Docker Compose Setup](#docker-compose-setup)
- [Authentication](#authentication)
- [API Endpoints](#api-endpoints)
- [Security](#security)
- [Configuration](#configuration)
- [Testing](#testing)

## Features

- **JWT Authentication & Authorization**
  - Secure user registration and login
  - Role-based access control (Customer/Admin)
  - Token-based stateless authentication
  - Comprehensive error handling

- **Customer Management**
  - User registration and profile management
  - Customer order history and reviews
  - Address management
  - Total spending tracking

- **Product Catalog**
  - Product browsing (public access)
  - Product management (admin only)
  - Category management
  - Stock management and sales tracking
  - Product reviews and ratings

- **Order Processing**
  - Order creation and management
  - Order item tracking
  - Order status management (process, ship, deliver, cancel)
  - Customer-specific order history

- **API Documentation**
  - Interactive Swagger UI
  - JWT authentication support in docs
  - Comprehensive API documentation

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security 6.x
- **Database**: PostgreSQL 15
- **Authentication**: JWT (JSON Web Tokens)
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Build Tool**: Maven
- **Containerization**: Docker & Docker Compose

## Getting Started

### Prerequisites

- Docker and Docker Compose
- Java 17+ (if running locally)
- Git

### Environment Variables

Create a `.env` file in the project root:

```env
# Database Configuration
DB_NAME=ecommerce_db
DB_USERNAME=ecommerce_user
DB_PASSWORD=password123

# Server Configuration
SERVER_PORT=8060

# JWT Configuration
JWT_SECRET=myVerySecureSecretKeyForJWTTokenGenerationThatShouldBeAtLeast256Bits
JWT_EXPIRATION=86400000

# Admin Configuration
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123

# Frontend URLs (for CORS)
FRONTEND_URL=http://localhost:3000
ADMIN_PANEL_URL=http://localhost:8080

# Logging Levels
LOG_LEVEL_ROOT=INFO
LOG_LEVEL_ECOMMERCE=DEBUG
LOG_LEVEL_SPRING_WEB=INFO
LOG_LEVEL_SECURITY=INFO
```

## Docker Compose Setup

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Ecommerce
   ```

2. **Create environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your configuration
   ```

3. **Start the application**
   ```bash
   docker-compose up -d
   ```

4. **Access the application**
   - **Main API**: http://localhost:8060
   - **Swagger Documentation**: http://localhost:8060/swagger-ui.html
   - **Health Check**: http://localhost:8060/health

### Docker Commands

```bash
# Start services
docker-compose up -d

# View logs
docker-compose logs -f app
docker-compose logs -f postgres

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up --build -d

# Clean up (removes volumes)
docker-compose down -v
```

### Database Setup

The PostgreSQL database will be automatically created with:
- Database: `ecommerce_db` (configurable)
- User: `ecommerce_user` (configurable)
- Port: `5432`
- Persistent data volume: `postgres_data`

## Authentication

### JWT Token Authentication

This application uses JWT tokens for stateless authentication.

#### 1. Register a New User
```bash
curl -X POST http://localhost:8060/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "message": "User registered successfully",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": "uuid-here",
  "username": "john@example.com",
  "name": "John Doe",
  "role": "CUSTOMER"
}
```

#### 2. Login
```bash
curl -X POST http://localhost:8060/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

#### 3. Use Token in Requests
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8060/api/customers/profile
```

## API Endpoints

### Authentication Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/api/auth/register` | Register new customer | Public |
| POST | `/api/auth/login` | Authenticate user and get JWT token | Public |
| POST | `/api/auth/validate` | Validate JWT token | Public |

### Customer Management Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| GET | `/api/customers` | Get all customers | Admin |
| POST | `/api/customers` | Create new customer | Admin |
| GET | `/api/customers/{id}` | Get customer by ID | Admin |
| PUT | `/api/customers/{id}` | Update existing customer | Customer/Admin |
| DELETE | `/api/customers/{id}` | Delete customer | Admin |
| GET | `/api/customers/email/{email}` | Get customer by email | Admin |
| GET | `/api/customers/name/{name}` | Get customer by name | Admin |
| GET | `/api/customers/high-value` | Get high value customers (by min amount) | Admin |
| GET | `/api/customers/exists/email/{email}` | Check if email exists | Public |
| GET | `/api/customers/{customerId}/addresses` | Get customer addresses | Customer/Admin |
| POST | `/api/customers/{customerId}/addresses` | Add address to customer | Customer/Admin |
| GET | `/api/customers/{customerId}/orders` | Get customer orders | Customer/Admin |
| GET | `/api/customers/{customerId}/orders/status/{status}` | Get customer orders by status | Customer/Admin |
| PUT | `/api/customers/{customerId}/total-spent` | Update total spent by customer | Admin |
| POST | `/api/customers/validate` | Validate customer data | Public |

### Product Management Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| GET | `/api/products` | Get all products | Public |
| POST | `/api/products` | Create new product | Admin |
| GET | `/api/products/{id}` | Get product by ID | Public |
| PUT | `/api/products/{id}` | Update existing product | Admin |
| DELETE | `/api/products/{id}` | Delete product | Admin |
| GET | `/api/products/{id}/summary` | Get product summary | Public |
| GET | `/api/products/paginated` | Get products with pagination | Public |
| GET | `/api/products/name/{name}` | Get product by name | Public |
| GET | `/api/products/search` | Search products by keyword | Public |
| GET | `/api/products/price-range` | Get products by price range | Public |
| GET | `/api/products/category/{categoryName}` | Get products by category | Public |
| GET | `/api/products/in-stock` | Get products in stock | Public |
| GET | `/api/products/out-of-stock` | Get out of stock products | Admin |
| GET | `/api/products/low-stock` | Get low stock products (by threshold) | Admin |
| GET | `/api/products/top-stock` | Get products with highest stock | Admin |
| GET | `/api/products/top-selling` | Get top selling products | Public |
| PUT | `/api/products/{productId}/stock` | Update product stock | Admin |
| PUT | `/api/products/{productId}/reduce-stock` | Reduce product stock | Admin |
| PUT | `/api/products/{productId}/increase-sales` | Increase sales count | Admin |
| GET | `/api/products/{productId}/reviews` | Get product reviews | Public |
| GET | `/api/products/{productId}/average-rating` | Get product average rating | Public |
| GET | `/api/products/{productId}/review-count` | Get product review count | Public |
| GET | `/api/products/{productId}/reviews/rating/{minRating}` | Get product reviews by min rating | Public |
| PUT | `/api/products/{productId}/categories/{categoryName}` | Add category to product | Admin |
| GET | `/api/products/exists/name/{name}` | Check if product name exists | Public |
| GET | `/api/products/{productId}/availability` | Check product availability | Public |
| POST | `/api/products/validate` | Validate product data | Admin |

### Order Management Endpoints
| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| GET | `/api/orders` | Get all orders | Admin |
| POST | `/api/orders` | Create new order | Customer/Admin |
| GET | `/api/orders/{id}` | Get order by ID | Customer/Admin |
| PUT | `/api/orders/{id}` | Update existing order | Admin |
| DELETE | `/api/orders/{id}` | Delete order | Admin |
| GET | `/api/orders/order-number/{orderNumber}` | Get order by order number | Customer/Admin |
| GET | `/api/orders/customer/{customerId}` | Get orders by customer | Customer/Admin |
| GET | `/api/orders/status/{status}` | Get orders by status | Admin |
| GET | `/api/orders/customer/{customerId}/status/{status}` | Get customer orders by status | Customer/Admin |
| GET | `/api/orders/{orderId}/items` | Get order items | Customer/Admin |
| POST | `/api/orders/{orderId}/items` | Add order item | Customer/Admin |
| GET | `/api/orders/items/product/{productName}` | Find order items by product name | Admin |
| POST | `/api/orders/create` | Create order with items | Customer/Admin |
| PUT | `/api/orders/{orderId}/status` | Update order status | Admin |
| PUT | `/api/orders/{orderId}/process` | Process an order | Admin |
| PUT | `/api/orders/{orderId}/ship` | Ship an order | Admin |
| PUT | `/api/orders/{orderId}/deliver` | Deliver an order | Admin |
| PUT | `/api/orders/{orderId}/cancel` | Cancel an order | Admin |
| GET | `/api/orders/{orderId}/total` | Calculate order total | Customer/Admin |
| GET | `/api/orders/{orderId}/item-count` | Get order item count | Customer/Admin |
| GET | `/api/orders/{orderId}/can-process` | Check if order can be processed | Admin |
| GET | `/api/orders/{orderId}/can-cancel` | Check if order can be cancelled | Customer/Admin |

## Security

### Role-Based Access Control

- **Public Access**: Product browsing, authentication, data validation, product reviews and ratings
- **Customer Access**: Profile management, order creation, personal data, addresses, personal orders
- **Admin Access**: All customer/product/order management operations, stock management, order processing, system analytics

### Security Features

- **JWT Token Authentication**: Stateless, secure token-based auth
- **Password Encryption**: BCrypt password hashing
- **CORS Support**: Configured for frontend integration
- **Method Security**: Annotation-based security for fine-grained control
- **Error Handling**: Proper JSON error responses for auth failures

### Security Headers

The application automatically includes security headers:
- `Authorization: Bearer <token>`
- CORS headers for cross-origin requests
- Content-Type validation

## Configuration

### Application Properties

Key configuration files:
- `application.yml` - Main configuration
- `application-test.yml` - Test environment
- `.env` - Environment variables (Docker)

### JWT Configuration

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
```

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecommerce_db
    username: ecommerce_user
    password: password123
```

## Testing

### Using Swagger UI

1. Navigate to http://localhost:8060/swagger-ui.html
2. Click "Authorize" button
3. Enter: `Bearer YOUR_JWT_TOKEN`
4. Test protected endpoints

### Using curl

```bash
# Get JWT token
TOKEN=$(curl -X POST http://localhost:8060/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}' \
  | jq -r '.token')

# Use token to access protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8060/api/customers/profile
```

### Health Check

```bash
curl http://localhost:8060/health
```

### Complete Workflow Example

```bash
# 1. Register a new customer
curl -X POST http://localhost:8060/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane@example.com",
    "password": "securepassword"
  }'

# 2. Login to get token
TOKEN=$(curl -X POST http://localhost:8060/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"jane@example.com","password":"securepassword"}' \
  | jq -r '.token')

# 3. Browse products (public)
curl http://localhost:8060/api/products

# 4. Search products by keyword
curl "http://localhost:8060/api/products/search?keyword=laptop"

# 5. Get product details
curl http://localhost:8060/api/products/{product-id}

# 6. Create an order with items (authenticated)
curl -X POST "http://localhost:8060/api/orders/create?customerId=YOUR_CUSTOMER_ID" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "productName": "Laptop",
      "quantity": 1,
      "price": 999.99
    }
  ]'

# 7. Check order status
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8060/api/orders/{order-id}"
```

## Troubleshooting

### Common Issues

1. **"Authentication token is missing"**
   - Ensure you're including the `Authorization: Bearer <token>` header

2. **"Access denied. Insufficient privileges"**
   - Check if your user role matches the endpoint requirements

3. **"Database connection failed"**
   - Verify PostgreSQL is running: `docker-compose ps postgres`

4. **CORS Issues**
   - Check that your frontend URL is in the allowed origins list

---

**Copyright © Wassim Lagnaoui 2025. All rights reserved.**
