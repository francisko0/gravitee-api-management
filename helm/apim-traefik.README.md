# Note of Traefik simple installation

Gravitee internal notes for Traefik simple installation on dev environment. 

### Add Traefik Helm Repository
helm repo add traefik https://traefik.github.io/charts
helm repo update

### Install Traefik Ingress Controller
Create namespace for Traefik if not exists: `kubectl create namespace apim-traefik`

```bash
helm install traefik traefik/traefik -f apim-traefik.values.yaml -n apim-traefik
```

### Install Traefik Resource Definitions:
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v3.2/docs/content/reference/dynamic-configuration/kubernetes-crd-definition-v1.yml

### Install RBAC for Traefik:
kubectl apply -f https://raw.githubusercontent.com/traefik/traefik/v3.2/docs/content/reference/dynamic-configuration/kubernetes-crd-rbac.yml

### Install Certificate for Traefik:
kubectl apply -f apim-traefik.certificate.yaml



