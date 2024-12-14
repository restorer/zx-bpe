#!/usr/bin/env python3

import os
import sys

WEBROOT = '.'


def main() -> int:
    resources_path = os.path.abspath(os.path.dirname(__file__) + '/../app/src/jsMain/resources')
    template_path = os.path.dirname(__file__) + '/service__template.js'
    result_path = resources_path + '/service.js'

    assets = f'    "{WEBROOT}/"'
    assets += f',\n    "{WEBROOT}/app.js"'

    for root, _, files in os.walk(resources_path):
        path = os.path.abspath(root)[(len(resources_path) + 1):]

        if path != '':
            path += '/'

        for file in files:
            assets += f',\n    "{WEBROOT}/{path}{file}"'

    with open(template_path, 'r', encoding='utf-8') as file:
        template = file.read()

    result = template.replace('/** ASSETS **/', assets)

    with open(result_path, 'w', encoding='utf-8') as file:
        file.write(result)

    print('Done')
    return 0

if __name__ == '__main__':
    sys.exit(main())
