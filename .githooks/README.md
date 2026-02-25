# Git Hooks

Questi hook impongono policy `feature/*`:

- `pre-commit`: blocca commit fuori da `feature/*`
- `pre-merge-commit`: blocca merge commit fuori da `feature/*`
- `pre-push`: blocca push verso `main|master|dev|develop`

Per attivarli nel repository locale:

```bash
./scripts/setup-git-hooks.sh
```

Override temporaneo (solo emergenza locale):

```bash
ALLOW_PROTECTED_BRANCH_COMMIT=1 git commit -m "..."
```
