folder("Whanos base images") {
	description("The base images of whanos.")
}

folder("Projects") {
	description("The available projets in whanos.")
}

languages = ["c", "java", "javascript", "python", "befunge"]

languages.each { language ->
	freeStyleJob("Whanos base images/whanos-$language") {
		steps {
			shell("docker build /var/lib/jenkins/images/$language -f /var/lib/jenkins/images/$language/Dockerfile.base -t whanos-$language")
		}
	}
}

freeStyleJob("Whanos base images/Build all base images") {
	publishers {
		downstream(
			languages.collect { language -> "Whanos base images/whanos-$language" }
		)
	}
}

freeStyleJob("link-project") {
	description('Clé SSH publique:\n\n{{public_key}}')
	parameters {
		stringParam("DISPLAY_NAME", null, "Display name for the job")
		stringParam("GIT_URL", null, 'Git repository url (e.g.: "https://github.com/Octopus773/ts-hello-world.git")')
		stringParam("GIT_BRANCH", "master", "Git branch to use")
		stringParam("REGISTRY_URL", "{{registry_domain}}", "Registry to store docker image")
	}
	steps {
		shell("ssh-keyscan github.com >> /var/lib/jenkins/.ssh/known_hosts")
		dsl {
			text('''
				freeStyleJob("Projects/$DISPLAY_NAME") {
					scm {
						git {
							remote {
								name("origin")
								branch("$GIT_BRANCH")
								url("$GIT_URL")
							}
						}
					}
					triggers {
						scm("* * * * *")
					}
					wrappers {
						preBuildCleanup()
					}
					steps {
						shell("/var/lib/jenkins/deploy.sh \\"$DISPLAY_NAME\\" \\"$REGISTRY_URL\\"")
					}
				}
			''')
		}
	}
}
