pipeline {
    agent any
    tools {
        maven "Maven"
    }

    options {
        skipStagesAfterUnstable()
    }
    parameters {
        booleanParam(name: "RELEASE",
                description: "Build a release from current commit.",
                defaultValue: false)
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage("Release") {
                    when {
                        expression { params.RELEASE }
                    }
                    steps {
                        sh "mvn -B release:prepare"
                        sh "mvn -B release:perform"
                    }
                }
    }

        post {
            always {
                deleteDir()
            }
        }
}