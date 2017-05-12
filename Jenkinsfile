#!groovy

pipeline {
    agent any
	tools { 
	        maven 'Maven 3.5.0' 
	        jdk 'jdk8' 
	}
    parameters {
        boolean(name: 'Release Build?', defaultValue: 'false', description: 'Should project be released?')
    }
    stages {
        stage('Unit Tests') {
            steps {
                sh 'mvn -B test'
                junit '**/target/surefire-reports/junitreports/*.xml'
            }
        }
        stage('Deploy Snapshot when on master branch'){
             when {
                 branch 'master'
             }
             steps {
                 sh 'mvn -B -DskipTests=true clean deploy'
             }
        }
    }
}
