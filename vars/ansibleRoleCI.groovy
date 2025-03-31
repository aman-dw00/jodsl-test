def call(String roleName, String repoUrl) {
    pipeline {
        agent any
        environment {
            ANSIBLE_STDOUT_CALLBACK = 'yaml'
        }
        stages {
            stage('Checkout') {
                steps {
                    git branch: 'main', url: repoUrl
                }
            }
            stage('Linting') {
                steps {
                    sh "ansible-lint roles/${roleName}"
                }
            }
            stage('Syntax Check') {
                steps {
                    sh "ansible-playbook --syntax-check roles/${roleName}/tests/test.yml"
                }
            }
            stage('Molecule Test') {
                steps {
                    sh "cd roles/${roleName} && molecule test"
                }
            }
            stage('Secret Scanning') {
                steps {
                    sh "gitleaks detect --source=roles/${roleName}"
                }
            }
        }
        post {
            always {
                archiveArtifacts artifacts: '**/*.log', allowEmptyArchive: true
            }
        }
    }
}
