#!groovy

pipeline {
    agent any
    tools {
        maven 'Maven 3.5.0'
        jdk 'jdk8'
    }
    parameters {
        booleanParam(name: 'Release Build?', defaultValue: false, description: 'Should project be released?')
    }
    stages {
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
                sh 'mvn -B clean -DskipTests=true clean deploy'
                withSonarQubeEnv('codequality.toolsmith.ch') {
                    sh 'mvn -B org.jacoco:jacoco-maven-plugin:prepare-agent test'
                    sh 'mvn -B sonar:sonar'
                }
            }
        }
    }
}
