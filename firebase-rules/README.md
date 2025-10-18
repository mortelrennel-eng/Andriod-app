This folder contains suggested Firebase security rules for Realtime Database and Storage.

Files:
- realtime.rules.json  : Realtime Database rules
- storage.rules        : Storage rules (example)

How to deploy (requires Firebase CLI):
1. Install and login: `npm install -g firebase-tools` ; `firebase login`
2. From project root, init rules if not already done: `firebase init` and choose 'database' and 'storage', or manually place these files under `firebase-rules` and configure firebase.json.
3. Deploy rules:
   firebase deploy --only database,storage --project <your-project-id>

Notes:
- The provided rules are examples and must be tested carefully. In particular, the storage.rules example uses Firestore lookup which may not be available in your RTDB-only project; adapt as needed.
- Consider using custom claims (via Admin SDK) for robust role checks instead of relying solely on user node fields.
 - The provided rules are examples and must be tested carefully.
 - For Storage rules we recommend using Firebase custom claims (set via Admin SDK or Cloud Functions) instead of reading RTDB nodes, because Storage rules cannot directly query Realtime Database. Example to set a custom claim (Node.js Admin SDK):

```js
admin.auth().setCustomUserClaims(uid, { role: 'superadmin' }).then(() => {
   // claim set
});
```

After setting claims, the storage rule can check `request.auth.token.role`.
