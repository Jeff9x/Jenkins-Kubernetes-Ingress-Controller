// Jenkinsfile
pipeline {
    agent any
    tools { 
         // This name must match the one in Global Tool Configuration
        docker 'default-docker' 
    }
    stages {
        stage('Build & Push App One') {
            steps {
                script {
                    sh 'docker build -t your-username/app-one ./app-one'
                    // Add push commands etc.

    environment {
        // !! CHANGE THIS VALUE !!
        DOCKER_HUB_USERNAME = "jeffare9x"
        KUBE_NAMESPACE = "default"
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
            }
        }
        stage('Build & Push App One') {
            steps {
                script {
                    def fullImageName = "${env.jeffare9x}/app-one"
                    def dockerImage = docker.build(fullImageName, './app-one')
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-creds') {
                        dockerImage.push("latest")
                    }
                }
            }
        }
        stage('Build & Push App Two') {
            steps {
                script {
                    def fullImageName = "${env.jeffare9x}/app-two"
                    def dockerImage = docker.build(fullImageName, './app-two')
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-creds') {
                        dockerImage.push("latest")
                    }
                }
            }
        }
        stage('Build & Push App Three') {
            steps {
                script {
                    def fullImageName = "${env.jeffare9x}/app-three"
                    def dockerImage = docker.build(fullImageName, './app-three')
                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-creds') {
                        dockerImage.push("latest")
                    }
                }
            }
        }
        stage('Deploy All Apps & Ingress') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
                    sh "kubectl apply -f k8s/deployment.yaml --namespace ${KUBE_NAMESPACE}"
                }
            }
        }
       // ... previous stages
            stage('Verify All Deployments') {
                steps {
                    script {
                        withCredentials([kubeconfigContent(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_CONTENT')]) {
                            sh '''
                                #!/bin/bash
                                echo "$KUBECONFIG_CONTENT" > kubeconfig
                                export KUBECONFIG=./kubeconfig
                                kubectl get deployments
                                kubectl get services
                                kubectl get ingress
                            '''
                        }
                    }
                }
            }
        }
    }
