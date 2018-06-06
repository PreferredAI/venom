---
name: Bug report
about: Create a report to help us improve

---

**Describe the bug**
A clear and concise description of what the bug is. If there's an exception/log add it here.

**To Reproduce**
Code/steps to reproduce the behavior:
```java
    String about = response.getJsoup().select(".about p").text();
    System.out.println("ABOUT: " + about);
```

**Expected behavior**
A clear and concise description of what you expected to happen.

**Desktop (please complete the following information):**
 - OS: [e.g. iOS]
 - Version [e.g. 22]

**Additional context**
Add any other context about the problem here.
