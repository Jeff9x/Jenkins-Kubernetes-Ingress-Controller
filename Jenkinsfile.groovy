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
        stage('Verify All Deployments') {
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-file', variable: 'KUBECONFIG')]) {
                    sh "kubectl rollout status deployment/app-one-deployment --namespace ${KUBE_NAMESPACE}"
                    sh "kubectl rollout status deployment/app-two-deployment --namespace ${KUBE_NAMESPACE}"
                    sh "kubectl rollout status deployment/app-three-deployment --namespace ${KUBE_NAMESPACE}"
                }
            }
        }
    }
}
