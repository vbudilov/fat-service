

```
# Build
docker build -t fat-service .

# Run
AWS_ACCESS_KEY_ID=$(aws --profile thepartials configure get aws_access_key_id)
AWS_SECRET_ACCESS_KEY=$(aws --profile thepartials configure get aws_secret_access_key)

docker run -it -p 18080:8080 --rm \
   -e AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID \
   -e AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY \
   fat-service

# Test
curl localhost:8080/ping

```
