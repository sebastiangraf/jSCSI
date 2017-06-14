#!groovy

pipeline {
    agent any
    tools {
        maven 'Maven 3.5.0'
        jdk 'jdk8'
    }
    parameters {
        booleanParam(name: 'release', defaultValue: false, description: 'Should project be released?')
    }
    stages {
        stage('When on master and release set, release') {
            when {
                expression {
                    GIT_BRANCH = 'origin/' + sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    sh 'echo $GIT_BRANCH'
                    sh 'echo params.release'
                    return GIT_BRANCH == 'origin/master' && params.release
                }
            }
            steps {
                sh 'git checkout master'
                sh 'echo "release comes here"'
            }
        }
        stage('Unit Tests') {
            steps {
                sh 'mvn -B test'
                junit '**/target/surefire-reports/junitreports/*.xml'
            }
        }
        stage('When on master, Deploy Snapshot and analyze for sonar') {
            when {
                branch 'master'
            }
            steps {
                sh 'mvn -B clean deploy -DskipTests=true'
                withSonarQubeEnv('codequality.toolsmith.ch') {
                    sh 'mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent test'
                    sh 'mvn -B sonar:sonar'
                }
            }
        }

    }
}
