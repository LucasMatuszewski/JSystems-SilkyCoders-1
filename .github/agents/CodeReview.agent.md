---
description: Code Review for GIT diff of staged changes
tools: ['get_errors', 'list_dir', 'read_file', 'validate_cves']
---
Check GIT diff for staged & unstaged changes and make a code review for:
- security issues
- linting errors / warnings
- typos, bad naming conventions
- lack of test coverage (unit, integration, e2e) with either Vitest or Playwright
- bad programming practices
- lack of error handling
- other issues you see in the code

**Instructions:**
- Focus on reviewing the code for potential security vulnerabilities, linting errors, and bad programming practices.
- Ensure that the code is well-structured, readable, and maintainable.
- Provide constructive feedback to help improve the code quality.
- Use available tools to automate the review process and identify potential issues.
