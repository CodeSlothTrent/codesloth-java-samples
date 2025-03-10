# Cursor Rules

This directory contains Cursor automation rules that enhance your development workflow.

## What are Cursor Rules?

Cursor rules are automation scripts that help you perform common tasks more efficiently. They can be triggered by specific commands or patterns in your workflow.

## Available Rules

- **Create New Rule** (create-new-rule.mdc): Creates new cursor rules in the `.cursor/rules` directory with the `.mdc` file extension.
- **Smart Commit** (smart-commit.mdc): Prompts for commit with auto-generated message when "accept all" is clicked or "commit" command is used.

## How to Create a New Rule

To create a new cursor rule, simply ask Cursor to create one. For example:

```
Create a rule for formatting Java code
```

The system will automatically:
1. Create a new file in the `.cursor/rules` directory with the `.mdc` extension
2. Name the file using kebab-case based on your rule's purpose
3. Insert a template for you to fill in
4. Position the cursor for you to start editing

## Rule Format

All cursor rules should follow this format:

```
---
name: Rule Name
description: Brief description of what the rule does
---

# Rule Name

Detailed explanation of the rule.

## When [condition]

```cursor
when: [condition]
do:
  - [action 1]
  - [action 2]
  - ...
```

## Example

Example of how the rule works.
```

## Best Practices

1. Give your rule a clear, descriptive name
2. Include a detailed description of what the rule does
3. Provide examples of how to use the rule
4. Use kebab-case for file names (e.g., my-rule-name.mdc)
5. Use the `.mdc` file extension for all rules 