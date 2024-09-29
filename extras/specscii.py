#!/usr/bin/env python3

import os
import sys
from PIL import Image


def main() -> int:
    image_path = os.path.dirname(__file__) + '/specscii__8x8.png'
    result_path = os.path.dirname(__file__) + '/../app/src/jsMain/kotlin/com/eightsines/bpe/util/SpecScii.kt'

    image = Image.open(image_path)
    pixels = image.load()

    (width, height) = image.size
    content_data = ''

    for row in range(0, height):
        if content_data != '':
            content_data += '\n'

        content_data += '        '
        need_separator = False

        for col in range(0, width):
            (_, val) = pixels[col, row]

            if need_separator:
                content_data += ', '

            content_data += '1' if val else '0'
            need_separator = True

        content_data += ','

    content = f'''package com.eightsines.bpe.util

object SpecScii {{
    const val WIDTH = {width}
    const val HEIGHT = {height}

    // @formatter:off
    val DATA: List<Int> = listOf(
{content_data}
    )
    // @formatter:on
}}
'''

    os.makedirs(os.path.dirname(result_path), exist_ok=True)

    with open(result_path, 'w') as file:
        file.write(content)

    print('Done')
    return 0

if __name__ == '__main__':
    sys.exit(main())
