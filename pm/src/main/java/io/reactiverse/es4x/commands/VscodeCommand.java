/*
 * Copyright 2019 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.reactiverse.es4x.commands;

import com.github.cliftonlabs.json_simple.JsonArray;
import com.github.cliftonlabs.json_simple.JsonObject;
import io.vertx.core.cli.CLIException;
import io.vertx.core.cli.annotations.*;
import io.vertx.core.spi.launcher.DefaultCommand;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static io.reactiverse.es4x.cli.Helper.*;

@Name("vscode")
@Summary("Launcher for vscode project.")
public class VscodeCommand extends DefaultCommand {

	private String launcher;

	@Option(longName = "launcher", shortName = "l")
	@Description("The launcher name")
  @DefaultValue("${workspaceFolder}/node_modules/.bin/es4x-launcher")
	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	private void processLauncher(File json) throws IOException {

    final File pkg = new File(getCwd(), "package.json");

    String app = "Launch";

    if (pkg.exists()) {
      JsonObject pkgJson = JSON.parse(pkg);
      app = "Launch " + pkgJson.get("name");
    }

    JsonObject launch = JSON.parse(json);

    if (!launch.containsKey("configurations")) {
			launch.put("configurations", new ArrayList<>());
		}

		final JsonArray configurations = (JsonArray) launch.get("configurations");

    // replace the launcher if already present
    Object toRemove = null;

    for (Object c : configurations) {
      JsonObject config = (JsonObject) c;
      if (app.equals(config.get("name"))) {
        toRemove = c;
        break;
      }
    }

    if (toRemove != null) {
      configurations.remove(toRemove);
    }

		Map<String, Object> config = new LinkedHashMap<>();
		config.put("name", app);
		config.put("type", "node");
		config.put("request", "launch");
		config.put("cwd", "${workspaceFolder}");
    config.put("runtimeExecutable", launcher);
		List<String> args = new ArrayList<>();
		if ("npm".equals(launcher)) {
		  // delegate to npm
      args.add("start");
      args.add("--");
    }
    if ("yarn".equals(launcher)) {
      // delegate to npm
      args.add("start");
    }
    args.add("-Dinspect=5858");
		config.put("runtimeArgs", args);
		config.put("port", 5858);
		config.put("outputCapture", "std");
		// server ready
    Map<String, Object> serverReady = new LinkedHashMap<>();
    serverReady.put("pattern", "started on port ([0-9]+)");
    serverReady.put("uriFormat", "http://localhost:%s");
    serverReady.put("action", "openExternally");
    config.put("serverReadyAction", serverReady);

		configurations.add(config);
    JSON.encode(json, launch);
	}

	@Override
	public void run() throws CLIException {
		File launch = new File(getCwd(), ".vscode/launch.json");

		if (!launch.exists()) {
		  final File vscode = new File(getCwd(), ".vscode");
			if (!vscode.exists() && !vscode.mkdirs()) {
        fatal("Failed to mkdir .vscode");
      }
			try (InputStream in = VscodeCommand.class.getClassLoader()
					.getResourceAsStream("META-INF/es4x-commands/vscode-launcher.json")) {
				if (in == null) {
					fatal("Cannot load vscode launcher.json template.");
				} else {
					Files.copy(in, launch.toPath());
				}
			} catch (IOException e) {
				fatal(e.getMessage());
			}
    }

    try {
      processLauncher(launch);
    } catch (IOException e) {
      fatal(e.getMessage());
    }
	}
}
