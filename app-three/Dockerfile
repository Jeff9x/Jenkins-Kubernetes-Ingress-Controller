name: Build Custom Jenkins Image

# Trigger this workflow on push to main or manually
on:
  push:
    branches: [ "main" ]
    paths:
      - 'Dockerfile' # Only run if the Dockerfile changes
  workflow_dispatch: # Allows manual triggering

jobs:
  build-and-push-image:
    name: Build and Push Jenkins Image
    runs-on: ubuntu-latest # Use a GitHub-hosted runner

    permissions:
      contents: read
      packages: write # Required to push to GHCR

    steps:
      - name: 1. Checkout repository
        uses: actions/checkout@v4

      - name: 2. Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: 3. Build and push custom Jenkins image
        uses: docker/build-push-action@v5
        with:
          context: . # Assumes Dockerfile is in the root
          file: ./Dockerfile # Explicitly points to your Dockerfile
          push: true
          tags: ghcr.io/${{ github.repository_owner }}/custom-jenkins:latest
