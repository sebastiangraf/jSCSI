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
        parallel{
        stage('Deploy Snapshot when on master branch'){
             when {
                 branch 'master'
             }
             steps {
                sh 'mvn -B clean -DskipTests=true clean deploy'
             }
        }
        stage('Make Sonar analysis') {
            when {
                branch 'master'
            }
            withSonarQubeEnv('codequality.toolsmith.ch') {
                sh 'mvn -B clean sonar:sonar'
            }
        }}
    }
}
