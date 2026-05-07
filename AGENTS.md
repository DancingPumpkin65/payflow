# AGENTS

I'm Badreddine. You're my agent. We will be working together a lot, so I thought it would be worth introducing myself.

I'm known for following youtube channel and work on "T3" products of Theo (t3.gg, T3 Chat, T3 Code, UploadThing, Ping, etc).

I love to build. I focus on building complex things as simple as possible. I love to find ways to reduce complexity when solving problems.

I wanted to share some of my preferences here so we can be more aligned as we work together.


## Coding preferences - general

- For every Payflow UI or interaction change, read and apply
  [.agents/skills/payflow-ui-motion/SKILL.md](.agents/skills/payflow-ui-motion/SKILL.md).
  Verify the affected navigation and its reverse on a rendered device or emulator.

- Keep things simple. Channel "yagni" energy unless told otherwise.

- Typesafety is useful, take advantage of it.

- Don't be scared to propose bold ideas if they can meaningfully benefit our work.

- Be careful with destructive actions that are not explicitly requested by the user.

- Tests should be focused, not slop. 

- Comments are a great way to clarify functionality and how code is used. Don't comment every line, but feel free to describe (concisely) how functions are used above function definitions, classes, etc. 

- Keep comments up to date! When making changes, it's important to keep things in sync.


## Coding preferences (Typescript focused)

- `any` is the enemy. Inferred types are our friend. Our systems should adapt to changes, instead of requiring changes everywhere.

- If your TS code looks like a Python dev wrote it, it is bad TS code.

- Avoid one-line functions that are just casting wrappers.

- Write TypeScript in ways that Matt Pocock and Theo would be proud of.

- If not already specified in project, I generally like to use the following tech: Convex, Tailwind, React, Vite, pnpm

- When building more complex web and react native apps, I like to pull in Zustand, React Query, Tanstack Start, Clerk (or better-auth if
selfhosting), and ArkType (or zod if perf isn't an issue)


## Questions are read-only

- A question is a request for an answer, not for changes. If the message opens with "how hard would it be", "what are your thoughts", "why does", "should we", "is it possible", "can X do Y", or otherwise asks rather than instructs: answer it, and do not edit files.

- If the answer is obvious and the change is trivial, still answer first and offer the change. Ask before making it.

## Commit history preferences

- Mandatory: use the user's exact date/count schedule in
  `scripts/commit-schedule.csv` for every new commit, including its Saturdays.
  These replace the earlier weekday-only preference. Never substitute today's
  date or invent a different schedule.
- Before committing, run `./scripts/commit-scheduled.ps1 -DryRun`. Stage only
  intended changes, then run `./scripts/commit-scheduled.ps1 -Message '...'`.
  The helper sets and verifies both author and committer dates in Morocco time.
  Do not bypass it with plain `git commit` or automatic GitHub merge commits;
  preserve this convention when making local merge commits too.
- Counts mean unique scheduled commits in this repository, including merges,
  across all local refs (not total GitHub account contributions). Fetch relevant
  refs first. May 4 already has its 11 commits. Consume remaining slots in order.
  If counts exceed a target or the schedule is exhausted, stop and report it.
- Commit completed logical changes with concise, specific messages. Multiple
  meaningful commits may share a day; do not create empty commits, arbitrary
  splits just to fill a quota. Unused scheduled slots remain for actual future
  work; do not claim that they have already been filled.
- Leave existing published history unchanged unless the user explicitly asks
  to rewrite it. Report the actual assigned dates when asked; backdated dates
  describe this requested history convention, not when the work was performed.

## Remote mobile access

- Keep replies and tool output concise. Do not print whole files, large diffs
  or lengthy build logs unless requested. Build and test on the host PC, save
  APKs locally, and finish with changes, test status and output location.
