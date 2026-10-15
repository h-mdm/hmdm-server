const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { test } = require('node:test');

const root = path.resolve(__dirname, '../../../..');
const read = file => fs.readFileSync(path.join(root, file), 'utf8');
const web = 'server/src/main/webapp';
const pluginWeb = plugin => `plugins/${plugin}/src/main/webapp`;
const constants = {};
const moduleStub = new Proxy({}, {
    get: (_, name) => (...args) => {
        if (name === 'constant') constants[args[0]] = args[1];
        return moduleStub;
    }
});
vm.runInNewContext(read(`${web}/app/app.js`), { angular: { module: () => moduleStub } });

test('Hungarian is registered, selectable and loaded by the entry page', () => {
    assert.equal(constants.SUPPORTED_LANGUAGES.hu, 'hu_HU');
    assert.equal(constants.SUPPORTED_LANGUAGES.hu_HU, 'hu_HU');
    assert.ok(constants.LOCALIZATION_BUNDLES.includes('hu_HU'));
    assert.equal(constants.APP_VERSION, '5.41.1');
    assert.match(read(`${web}/index.html`), /src='localization\/hu_HU\.js'/);
    assert.match(read(`${web}/app/components/main/view/settings/language.html`), /ng-value="'hu_HU'"/);
});

test('Hungarian covers current application keys and preserves substitution tokens', () => {
    const context = { document: { localization: {} } };
    for (const locale of constants.LOCALIZATION_BUNDLES) {
        vm.runInNewContext(read(`${web}/localization/${locale}.js`), context);
        assert.equal(context.document.localization[locale]['form.settings.lang.hu'], 'Magyar');
    }
    const english = context.document.localization.en_US;
    const hungarian = context.document.localization.hu_HU;
    const tokens = text => [...new Set(text.match(/\$\{[^}]+\}|%[A-Z0-9_]+%/g) || [])].sort();
    for (const [key, value] of Object.entries(english)) {
        assert.ok(hungarian[key], `Missing Hungarian key: ${key}`);
        assert.deepEqual(tokens(hungarian[key]), tokens(value), key);
    }
});

test('Hungarian plugin bundles cover English keys and every locale labels MAC addresses', () => {
    for (const plugin of ['audit', 'deviceinfo', 'devicelog', 'messaging', 'push']) {
        const english = JSON.parse(read(`${pluginWeb(plugin)}/i18n/en_US.json`));
        const hungarian = JSON.parse(read(`${pluginWeb(plugin)}/i18n/hu_HU.json`));
        for (const key of Object.keys(english).filter(Boolean)) {
            assert.ok(hungarian[key], `Missing ${plugin} Hungarian key: ${key}`);
        }
    }
    for (const locale of constants.LOCALIZATION_BUNDLES) {
        const bundle = JSON.parse(read(`${pluginWeb('deviceinfo')}/i18n/${locale}.json`));
        assert.ok(bundle['plugin.deviceinfo.title.mac'], locale);
    }
    assert.match(read(`${pluginWeb('deviceinfo')}/views/info.html`), /{{deviceInfo\.mac}}/);
});

test('limited-user selectors use the configuration-name endpoint', () => {
    for (const [plugin, expected] of [['messaging', 1], ['push', 2]]) {
        const source = read(`${pluginWeb(plugin)}/${plugin}.module.js`);
        new vm.Script(source);
        assert.equal((source.match(/configurationService\.getAllConfigNames\(/g) || []).length, expected);
        assert.doesNotMatch(source, /configurationService\.getAllConfigurations\(/);
    }
    assert.match(read(`${web}/app/components/main/service/main.service.js`),
        /getAllConfigNames:\s*\{url:\s*'rest\/private\/configurations\/list'/);
});
