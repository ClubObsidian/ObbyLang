/*
 *     ObbyLang
 *     Copyright (C) 2021 virustotalop
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

(function () {
	const File = Java.type("java.io.File");
	const String = Java.type("java.lang.String");
	const Scanner = Java.type("java.util.Scanner");
	const PackageResolver = Java.type("com.clubobsidian.obbylang.project.PackageResolver");
	const SLASH = Packages.java.io.File.separator;
	const require = this.require = function (id) {
		/*debug*/
		//console.log('require('+id+')');
		if (typeof arguments[0] !== 'string' || arguments.length !== 1) {
			throw 'USAGE: require(moduleId)';
		}
		/*if(PROJECT_DIRECTORY !== null && PROJECT_DIRECTORY !== undefined) {
            id = PROJECT_DIRECTORY.getAbsolutePath() + SLASH + id
            log.info('id: ' + id);
		}*/
		const moduleUri = require.resolve(id);
		log.info('moduleUri: ' + moduleUri);
		let moduleContent = '';
		if (require.cache[moduleUri]) {
			return require.cache[moduleUri];
		}
		const file = new File(moduleUri);
		log.info("file location: " + file.getAbsolutePath());
		try {
			const scanner = new Scanner(file).useDelimiter('\\Z');
			moduleContent = new String(scanner.next());
		} catch (e) {
			throw 'Unable to read file at: ' + moduleUri + ', ' + e;
		}
		let exports;
		if (moduleContent) {
			try {
				const f = new Function('require', 'exports', 'module', moduleContent);
				exports = require.cache[moduleUri] || {}, module = {
					id: id,
					uri: moduleUri,
					exports: exports
				};
				require._root.unshift(moduleUri);
				f.call({}, require, exports, module);
				require._root.shift();
			} catch (e) {
				throw 'Unable to require source code from "' + moduleUri + '": ' + e.toSource();
			}
			exports = module.exports || exports;
			require.cache[moduleUri] = exports;
		} else {
			throw 'The requested module cannot be returned: no content for id: "' + id + '" in paths: ' + require.paths.join(', ');
		}
		return exports;
	};
	require._root = [''];
	require.paths = [];
	require.cache = {};
	// cache module exports. Like: {id: exported}
	/** Given a module id, try to find the path to the associated module.
	 */
	require.resolve = function (id) {
		// TODO: 1. load node core modules
		// 2. dot-relative module id, like './foo/bar'
		const parts = id.match(/^(\.?\.(?:\\|\/)|(?:\\|\/))(.+)$/);
		let isRelative = false;
		let = isAbsolute = false;
		let basename = id;
		if (parts) {
			isRelative = parts[1] === './' || parts[1] === '.\\' || parts[1] === '../' || parts[1] === '..\\';
			isAbsolute = parts[1] === '/' || parts[1] === '\\';
			basename = parts[2];
		}
		if (typeof basename !== 'undefined') {
			if (isAbsolute) {
				rootedId = id;
			} else {
				var root = isRelative ? toDir(require._root[0] || '.') : '.', rootedId = deDotPath(root + SLASH + id), uri = '';
			}
			if (uri = loadAsFile(rootedId)) {
			} else if (uri = loadAsDir(rootedId)) {
			} else if (uri = loadNodeModules(rootedId)) {
			} else if (uri = nodeModulesPaths(rootedId, 'rhino_modules')) {
			} else if (uri = nodeModulesPaths(rootedId, 'node_modules')) {
			}
			if (uri !== '') {
				return toAbsolute(uri);
			}
			throw 'Require Error: Not found.';
		}
	};
	/** Given a path, return the base directory of that path.
		@example toDir('/foo/bar/somefile.js'); => '/foo/bar'
	 */
	function toDir(path) {
		if (isDir(path)) {
			return path;
		}
		const parts = path.split(/[\\\/]/);
		parts.pop();
		return parts.join(SLASH);
	}
	/** Returns true if the given path exists and is a file.
	 */
	function isFile(path) {
		const file = new File(path);
		return file.isFile()
	}
	/** Returns true if the given path exists and is a directory.
	 */
	function isDir(path) {
		const file = new File(path);
		return file.isDirectory();
	}
	/** Get the path of the current working directory
	 */
	function getCwd() {
		return deDotPath(toDir('' + new File('.').getAbsolutePath()));
	}
	function toAbsolute(relPath) {
		let absPath = '' + new File(relPath).getAbsolutePath();
		absPath = deDotPath(absPath);
		return absPath;
	}
	function deDotPath(path) {
		return new String(path).replace(/(\/|\\)[^\/\\]+\/\.\.(\/|\\)/g, SLASH).replace(/(\/|\\)\.(\/|\\|$)/g, SLASH);
	}
	/** Assume the id is a file, try to find it.
	 */
	function loadAsFile(id) {
		if (isFile(id)) {
			return id;
		} else if (isFile(id + '.js')) {
			return id + '.js';
		} else if (isFile(id + '.node')) {
			throw 'Require Error: .node files not supported';
		}
	}
	/** Assume the id is a directory, try to find a module file within it.
	 */
	function loadAsDir(id) {
		if (!isDir(id)) {
			return;
		}
		const packageOpt = PackageResolver.resolve(new File(id));
		if (packageOpt.isPresent()) {
		    const projectPackage = packageOpt.get();
		    const mainName = projectPackage.getMain();
			if (mainName !== null) {
				const main = deDotPath(id + SLASH + mainName);
				return require.resolve(main);
			}
		}
		if (isFile(id + SLASH + 'index.js')) {
			return id + SLASH + 'index.js';
		}
	}
	function loadNodeModules(id) {
		const cwd = getCwd();
		const dirs = cwd.split(SLASH);
		const dir = dirs.join(SLASH);
		for (let i = 0, len = require.paths.length; i < len; i++) {
			let path = require.paths[i];
			path = deDotPath(dir + SLASH + path);
			if (isDir(path)) {
				path = deDotPath(path + SLASH + id);
				let uri = loadAsFile(path);
				if (typeof uri !== 'undefined') {
					return uri;
				}
				uri = loadAsDir(path);
				if (typeof uri !== 'undefined') {
					return uri;
				}
			}
		}
	}
	function nodeModulesPaths(id, moduleFolder) {
		const cwd = getCwd();
		const dirs = cwd.split(SLASH);
		let uri;
		while (dirs.length) {
			let dir = dirs.join(SLASH);
			let path = dir + SLASH + moduleFolder;
			if (isDir(path)) {
				const filename = deDotPath(path + SLASH + id);
				if (uri = loadAsFile(filename)) {
					uri = uri.replace(cwd, '.');
					return uri;
				} else if (uri = loadAsDir(filename)) {
					uri = uri.replace(cwd, '.');
					return uri;
				}
			}
			dirs.pop();
		}
	}
}());