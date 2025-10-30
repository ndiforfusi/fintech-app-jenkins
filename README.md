# fintech-app-jenkins
## CI/CD (Maven → ECR → EKS) via Jenkins

This guide explains how to implement, run, and maintain the Jenkins Declarative Pipeline that replaces your GitHub Actions workflow. It builds the app with Maven, runs SonarQube analysis, pushes a versioned Docker image to ECR, and deploys to EKS via kustomize overlays.

1) What the Pipeline Does

Build & package Java app (JDK 17, Maven)

Static analysis via SonarQube (token or plugin)

Generate or accept an image tag (timestamp fallback)

Authenticate to AWS via STS AssumeRole (recommended) or static AWS keys

Build & push Docker image to Amazon ECR

Deploy to Amazon EKS (update overlay’s patch-deployment.yaml, apply kustomize overlays)

Safeguards: manual approval gate on main/release, PRs skip deploy, concurrency disabled

Notifications: Slack webhook on success/failure

Hygiene: artifact JARs, clean Docker/cache, redact secrets

2) Repository Layout
.
├─ Jenkinsfile
├─ pom.xml
├─ src/...
├─ eks_addons/
│  ├─ script/
│  │  ├─ helm_install.sh
│  │  └─ helm_charts.sh
│  ├─ monitoring/   (kustomize)
│  └─ elk/          (kustomize)
├─ k8s/
│  └─ overlays/
│     ├─ dev/
│     │  ├─ kustomization.yaml
│     │  └─ patch-deployment.yaml
│     ├─ qa/
│     ├─ uat/
│     └─ prod/
└─ fintech-app/ (optional alternative path for overlays)


The pipeline searches for patch-deployment.yaml and overlay dirs at:

./k8s/overlays/$ENV

./fintech-app/k8s/overlays/$ENV

/fintech-app/k8s/overlays/$ENV

3) Jenkins Prerequisites
Plugins

Pipeline (workflow-aggregator)

Git or GitHub Branch Source (for Multibranch)

Credentials Binding

AWS Steps (for withAWS AssumeRole)

AnsiColor

Timestamper

(Optional) Slack plugin — not required if you use webhook as in the Jenkinsfile

(Optional) SonarQube Scanner plugin (alternatively use Maven sonar goal + token)

Agents / Tooling

Linux agent(s) with:

JDK 17 (Temurin/OpenJDK)

Maven 3.9+

Docker (build & push)

AWS CLI v2

kubectl

(optional) helm, kustomize (kustomize is part of kubectl -k)

Ensure the Jenkins agent user can access Docker (docker group or rootless podman adaptation).

Credentials (Jenkins → Manage Credentials)

aws-static-creds (AWS Credentials, only if not using AssumeRole)

SONAR_HOST_URL (Secret text) — e.g., https://sonar.myorg.com

SONAR_TOKEN (Secret text)

slack-webhook-url (Secret text) — your Slack Incoming Webhook

Recommended: Use STS AssumeRole (set ASSUME_ROLE_ARN param). The AWS credential used by Jenkins (controller/agent) must be allowed to sts:AssumeRole on the target role.

4) Job Setup
Multibranch Pipeline (recommended)

New Item → Multibranch Pipeline

Set GitHub or Git (repo URL + credentials if needed)

Save & Scan Multibranch

Jenkins auto-creates jobs for each branch & PR

Classic Pipeline (single)

New Item → Pipeline

Pipeline script from SCM → repo URL, Jenkinsfile

Enable This project is parameterized (Jenkinsfile already defines params)

5) Parameters

ENVIRONMENT: dev|qa|uat|prod

IMAGE_TAG: optional; leave blank to auto-generate UTC timestamp (e.g., 20251030123456)

REGION: default us-east-2

ECR_REPO: default fintech-app

ASSUME_ROLE_ARN: target role to assume (preferred)

AWS_ACCOUNT_ID_FALLBACK: used only if STS is unavailable

6) Branch & PR Behavior

PR builds: build (and optionally Sonar) — no deploy.

main / release branches: build → approval gate → deploy to EKS.

Concurrency is disabled to avoid overlapping builds on the same job.

Adjust the when conditions in Deploy Gate and Deploy to EKS stages if you want different rules (e.g., only release deploys).

7) AWS Authentication & Account Resolution

The Jenkinsfile:

Tries AssumeRole (withAWS) if ASSUME_ROLE_ARN provided.

Otherwise uses a static credential binding (aws-static-creds).

Resolves Account ID via aws sts get-caller-identity.

Falls back to AWS_ACCOUNT_ID_FALLBACK if STS is not available.

Ensure the ECR registry exists in the resolved account (${account}.dkr.ecr.${region}.amazonaws.com) and the repository ${ECR_REPO} is created (you can aws ecr create-repository once).

8) EKS Deployment (kustomize overlays)

Cluster is named ${ENVIRONMENT}-dominion-cluster (customize this in Jenkinsfile if yours differs).

The pipeline:

aws eks update-kubeconfig

Optionally runs add-on shell scripts (helm install & charts)

Patches the image in patch-deployment.yaml

Runs kubectl apply -k <overlay dir>

Requirement: Your overlay’s deployment must reference the same container that the patch adjusts.

9) Slack Notifications

Success/Failure notifications are sent via webhook using credential slack-webhook-url.

Update the helper slackNotify or swap to the Slack plugin if preferred.

10) Security Best Practices

Prefer STS AssumeRole over long-lived keys.

Scope IAM policies minimally (ECR push, EKS describe/update-kubeconfig, any AWS APIs needed by your app).

Protect secrets in Jenkins folders; use Role-Based Access Control.

Restrict kubectl permissions to target namespaces/ops via IRSA or cluster roles.

Consider adding image scanning, OPA Gatekeeper/Kyverno policies, and admission controls.

11) Troubleshooting

Cannot resolve account ID: verify AWS auth works (STS). Otherwise set a valid AWS_ACCOUNT_ID_FALLBACK.

ECR login fails: ensure repository exists and the IAM principal can ecr:GetAuthorizationToken, ecr:InitiateLayerUpload, etc.

kubectl fails: verify cluster name, region, and access (aws-auth, access entries, or IAM authenticator).

SonarQube auth: confirm SONAR_HOST_URL and SONAR_TOKEN credentials are set; server reachable from the agent.

Overlay not found: ensure one of the expected overlay paths exists.

12) Optional Enhancements

Quality gates: integrate mvn test, jacoco, spotbugs, checkstyle, owasp-dependency-check.

PR feedback: post plan/lint results back to GitHub (via Checks API) or Slack threads.

Blue/Green or Canary: drive progressive delivery via separate overlays and Service selectors.

Rollback: store previous image tag/build number; add a parameterized rollback stage.

Quick Start Checklist

 Jenkins agent has JDK 17, Maven 3.9+, Docker, AWS CLI, kubectl

 Credentials added: slack-webhook-url, SONAR_HOST_URL, SONAR_TOKEN, and (optionally) aws-static-creds

 ECR repo ${ECR_REPO} exists in ${REGION}

 EKS cluster ${ENVIRONMENT}-dominion-cluster reachable; kubeconfig update allowed

 Overlays present with patch-deployment.yaml

 (Preferred) ASSUME_ROLE_ARN set; trust policy allows Jenkins principal to assume
