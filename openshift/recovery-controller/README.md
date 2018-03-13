# Spring-Boot Narayana Recovery Controller

The Spring-Boot Narayana recovery controller allows to gracefully handle the scaling down phase of a StatefulSet
by "cleaning up" pending transactions before termination.

If a scaling down operation is executed and the pod is not clean after termination, the previous number of replicas is restored, 
hence effectively cancelling the scaling down operation.

All pods of the statefulset require access to a shared volume that is used to store the termination status of each pod belonging to the StatefulSet.

The pod-0 of the StatefulSet periodically checks the status and scale the StatefulSet to the right size if there's a mismatch.

A "edit" role on the namespace is required on Openshift in order for the pod-0 to change the number of replicas. 

## Configuration

Configuring Narayana to work on Openshift with the recovery controller requires special care. The following snipped shows an example of `application.properties`:

```properties
# You need to replace the following options in the Kubernetes yaml descriptor, see below
cluster.nodename=1
cluster.base-dir=./target/tx

# TX manager
spring.jta.transaction-manager-id=${cluster.nodename}
spring.jta.log-dir=${cluster.base-dir}/store/${cluster.nodename}

# Narayana recovery settings
snowdrop.narayana.openshift.recovery.enabled=true
snowdrop.narayana.openshift.recovery.current-pod-name=${cluster.nodename}
# You must enable resource filtering in order to inject the Maven artifactId
snowdrop.narayana.openshift.recovery.statefulset=${project.artifactId}
snowdrop.narayana.openshift.recovery.status-dir=${cluster.base-dir}/status
```

You need a shared volume to store both transactions and termination info. It can be mounted in the statefulset yaml descriptor:

```yaml
apiVersion: apps/v1beta1
kind: StatefulSet
#...
spec:
#...
  template:
#...
    spec:
      containers:
      - env:
        - name: CLUSTER_BASE_DIR
          value: /var/transaction/data
          # Override CLUSTER_NODENAME with Kubernetes Downward API (to use `pod-0`, `pod-1` etc. as tx manager id)
        - name: CLUSTER_NODENAME
          valueFrom:
            fieldRef:
              apiVersion: v1
              fieldPath: metadata.name
#...
        volumeMounts:
        - mountPath: /var/transaction/data
          name: the-name-of-the-shared-volume
#...
``` 

## Camel Extension for Spring-Boot Narayana Recovery Controller 

If Camel is found in the Spring-Boot application context, the Camel context is automatically stopped before 
flushing all pending transactions.
