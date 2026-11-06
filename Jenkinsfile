@Library('nstut-ci') _

nstutGradle(
    targets: [
        [name: 'Forge 1.20.1 tests and GameTests', java: 17, minutes: 60,
         command: './gradlew :forge-1.20.1:testClasses :forge-1.20.1:assemble :forge-1.20.1:runGameTestServer --no-daemon --max-workers=1 --stacktrace'],
        [name: 'NeoForge 1.21.1', java: 21, minutes: 45,
         command: './gradlew :neoforge-1.21.1:test :neoforge-1.21.1:build --no-daemon --max-workers=1 --stacktrace'],
        [name: 'NeoForge 26.1.2', java: 25, minutes: 45,
         command: './gradlew :neoforge-26.1.2:test :neoforge-26.1.2:build --no-daemon --max-workers=1 --stacktrace']
    ],
    tests: '**/build/test-results/test/*.xml',
    artifacts: '**/build/libs/*.jar',
    diagnostics: '**/build/reports/**,**/run/logs/**'
)

