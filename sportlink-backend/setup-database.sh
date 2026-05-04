#!/bin/bash

echo "Setting up PostgreSQL database for Spring Boot application..."
echo ""

# Check if PostgreSQL is running
if systemctl is-active --quiet postgresql; then
    echo "✓ PostgreSQL is running"
else
    echo "✗ PostgreSQL is not running"
    echo "  Please start PostgreSQL: sudo systemctl start postgresql"
    exit 1
fi

# Create the database
echo ""
echo "Creating database 'esi2023'..."
sudo -u postgres psql -c "CREATE DATABASE esi2023;" 2>/dev/null

if [ $? -eq 0 ]; then
    echo "✓ Database 'esi2023' created successfully"
else
    echo "ℹ Database 'esi2023' might already exist (this is okay)"
fi

# Verify the database exists
echo ""
echo "Verifying database..."
sudo -u postgres psql -lqt | cut -d \| -f 1 | grep -qw esi2023

if [ $? -eq 0 ]; then
    echo "✓ Database 'esi2023' exists and is ready"
else
    echo "✗ Database verification failed"
    exit 1
fi

echo ""
echo "✓ Database setup complete!"
echo ""
echo "Next steps:"
echo "1. Run the application: ./mvnw spring-boot:run"
echo "2. Test the API at: http://localhost:8080/products"
