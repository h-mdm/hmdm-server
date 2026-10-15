# Using Claude Code to implement custom features

## Prerequisites

### Claude Code command line

The recommended OS for building Headwind MDM is Ubuntu Linux (24.04 LTS is the best option).

Install Claude:

    curl -fsSL https://claude.ai/install.sh | bash
	export PATH="$HOME/.local/bin:$PATH"

Run Claude in the project directory, authorize, and confirm that you trust the project..

### Claude Code Web Interface

- Push the project to github.com

  Make the project private.


- Install the Claude app on github.com

  Select **Settings - Applications** and install the **Claude** app.


- Authorize the app to send Github projects to the Claude web interface.

  If the Claude app is already installed and you created a new project, you may still need to
  authorize the Claude app to send the project to Claude. Select **Settings - Applications**,
  ""Claude"", Configure, Repository Access, and select the new repository.

- Connect to the project

  In the Claude Code web interface, create a new session, click + to
  connect to a project, and choose the project.

## Implementation workflow

- Write the requirements

  - If you're developing a new plugin, create the `plugins/newplugin`
  subdirectory and store the requirements in the `plugins/newplugin/CLAUDE.md` file.

  - If you're adding a feature to the main module, store the requirements
  in the `docs/newfeature.md` file.

- Specify the purpose, the database schema, and API endpoints.
- Once the requirements are written, prompt Claude to review them and highlight
missing points.

> Load the project. Review the project and the documentation. Is the provided information enough to start building?
> If there are missing points or contradictions - write them to the chat for clarification.

- Adjust the requirements, fixing the missing points and contradictions.

> Pull the changes from the remote repository. Check whether the mentioned missing points are resolved.

- Once Claude confirms that requirements are fine, prompt Claude to prepare
the build plan and specify the list of created/updated files.

> Write a build plan. List the generated files and briefly describe their purpose.

- Adjust the requirements if the build plan doesn't meet your expectations.
- Once the plan is ready, prompt Claude to implement the feature.

*Notice: the `docs/BEHAVIOR.md` file requires Claude to be command-driven -
it shouldn't build anything without your command.*

- The implemented feature will be pushed into a branch.
- Merge the changes into the main branch only after testing.

*For quick fixes, we recommend to use ChatGPT and manual fixes, as this will generally be faster than using Claude.*
