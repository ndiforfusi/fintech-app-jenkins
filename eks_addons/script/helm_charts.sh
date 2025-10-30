#!/bin/bash

set -euo pipefail

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Ensure Helm is installed
if ! command_exists helm; then
    echo "❌ Helm is not installed. Please install Helm before running this script."
    exit 1
fi

# Ensure kubectl is installed
if ! command_exists kubectl; then
    echo "❌ kubectl is not installed. Please install kubectl before running this script."
    exit 1
fi

# Add Grafana Helm repository if not present
echo "📦 Adding Grafana Helm repository..."
if helm repo list | grep -q "^grafana"; then
    echo "✅ Grafana repository already exists. Skipping."
else
    helm repo add grafana https://grafana.github.io/helm-charts
fi

# Add Prometheus Helm repository if not present
if helm repo list | grep -q "^prometheus-community"; then
    echo "✅ Prometheus repository already exists. Skipping."
else
    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
fi

echo "🔄 Updating Helm repositories..."
helm repo update

# Create 'monitoring' namespace if it doesn't exist
echo "📁 Creating namespace 'monitoring' if not exists..."
kubectl create namespace monitoring --dry-run=client -o yaml | kubectl apply -f -

# Check if prometheus-stack is already installed
if helm list -n monitoring | grep -q "^prometheus-stack"; then
    echo "✅ prometheus-stack is already installed in the 'monitoring' namespace. Skipping installation."
else
    echo "🚀 Installing prometheus-stack via Helm..."
    helm install prometheus-stack prometheus-community/kube-prometheus-stack \
        --namespace monitoring \
        --set grafana.enabled=true
fi

# Wait for Grafana deployment to be ready
echo "⏳ Waiting for Grafana deployment to become ready..."
GRAFANA_DEPLOY=$(kubectl get deployment -n monitoring -l app.kubernetes.io/name=grafana -o jsonpath="{.items[0].metadata.name}")

if [ -n "$GRAFANA_DEPLOY" ]; then
    kubectl rollout status deployment/"$GRAFANA_DEPLOY" -n monitoring --timeout=300s
else
    echo "❌ Could not find Grafana deployment in 'monitoring' namespace."
    kubectl get all -n monitoring
    exit 1
fi

# Output Grafana service info
echo "📊 Grafana deployment complete!"
kubectl get svc -n monitoring -l app.kubernetes.io/name=grafana
