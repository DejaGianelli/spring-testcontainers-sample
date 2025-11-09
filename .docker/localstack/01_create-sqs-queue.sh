#!/bin/bash

awslocal --endpoint-url=http://localhost:4566 sqs create-queue \
    --queue-name order-placement