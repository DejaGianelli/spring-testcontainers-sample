#!/bin/bash

awslocal --endpoint-url=http://localhost:4566 secretsmanager create-secret \
  --name /secrets/database-secrets \
  --secret-string '{"username":"root","password":"example","database":"spring_testing","port":"3306"}'