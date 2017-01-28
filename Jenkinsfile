#!groovy

pipeline {
    agent any
	tools { 
	        maven 'Maven 3.5.0' 
	        jdk 'jdk8' 
	}
    stages {
        stage('build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }
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

	post {
	    always {
	        echo 'This will always run'
	    }
	    success {
	        echo 'This will run only if successful'
	    }
	    failure {
	        echo 'This will run only if failed'
	    }
	    unstable {
	        echo 'This will run only if the run was marked as unstable'
	    }
	    changed {
	        echo 'This will run only if the state of the Pipeline has changed'
	        echo 'For example, if the Pipeline was previously failing but is now successful'
	    }
	}	
}