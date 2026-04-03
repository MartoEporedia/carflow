# Security Policy

## Supported Versions

We currently support the latest version for security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.x     | :white_check_mark: |

## Reporting a Vulnerability

We take security issues seriously. If you discover a vulnerability, please report it **privately**:

**DO NOT** open a public GitHub issue for security vulnerabilities.

### How to Report

1. Email: [INSERT SECURITY EMAIL OR CONTACT INFO]
2. Include:
   - Description of vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### What to Expect

- We will acknowledge receipt within 48 hours
- We will provide a timeline for a fix within 5 days
- We will coordinate disclosure once a patch is ready

## Security Best Practices for Users

- Keep your device and OS updated
- Only install APKs from official sources
- The app stores data locally; no data is transmitted externally
- Ensure device lock screen is enabled

## Data Handling

CarFlow stores all expense data locally in an encrypted Room database (SQLCipher optional in future). No data is collected, transmitted, or shared with third parties.

## Known Limitations

- Backup to cloud is not implemented - users are responsible for their own data backup
- No authentication - anyone with device access can view expenses
