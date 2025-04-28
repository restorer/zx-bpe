#!/usr/bin/env python3

import heapq
import os
import sys


TYPE_NULL = '_'
TYPE_BOOLEAN_FALSE = 'b'
TYPE_BOOLEAN_TRUE = 'B'
TYPE_INT_4 = 'i'
TYPE_INT_8 = 'I'
TYPE_INT_16 = 'n'
TYPE_INT_32 = 'N'
TYPE_STRING_4 = 's'
TYPE_STRING_8 = 'S'
TYPE_STRING_16 = 't'
TYPE_STRING_32 = 'T'
TYPE_STUFF_4 = 'u'
TYPE_STUFF_8 = 'U'
TYPE_STUFF_16 = 'f'
TYPE_STUFF_32 = 'F'

STATS_NULL = 'null'
STATS_BOOLEAN = 'bool'
STATS_INT_4 = 'int4'
STATS_INT_8 = 'int8'
STATS_INT_16 = 'int16'
STATS_INT_32 = 'int32'
STATS_STRING_4 = 'string4'
STATS_STRING_8 = 'string8'
STATS_STRING_16 = 'string16'
STATS_STRING_32 = 'string32'
STATS_STUFF_4 = 'stuff4'
STATS_STUFF_8 = 'stuff8'
STATS_STUFF_16 = 'stuff16'
STATS_STUFF_32 = 'stuff32'


class BagException(Exception):
    def __init__(self, message: str):
        super().__init__(message)


def create_empty_stats() -> dict:
    return {
        STATS_NULL: 0,
        STATS_BOOLEAN: 0,
        STATS_INT_4: 0,
        STATS_INT_8: 0,
        STATS_INT_16: 0,
        STATS_INT_32: 0,
        STATS_STRING_4: 0,
        STATS_STRING_8: 0,
        STATS_STRING_16: 0,
        STATS_STRING_32: 0,
        STATS_STUFF_4: 0,
        STATS_STUFF_8: 0,
        STATS_STUFF_16: 0,
        STATS_STUFF_32: 0,
    }


def read_number(data: str, index: int, chars: int) -> tuple:
    result = int(data[index : index + chars], 16)
    return (result, index + chars)


def compute_stats(path: str) -> dict:
    with open(path, 'r') as f:
        data = f.read()

    if not data.startswith('BAG1'):
        raise BagException(f'Unknown signature at path="{path}"')

    stats = create_empty_stats()
    index = 4

    while index < len(data):
        ch = data[index]
        index += 1

        if ch == TYPE_NULL:
            stats[STATS_NULL] += 1
        elif ch == TYPE_BOOLEAN_FALSE or ch == TYPE_BOOLEAN_TRUE:
            stats[STATS_BOOLEAN] += 1
        elif ch == TYPE_INT_4:
            (_, index) = read_number(data, index, 1)
            stats[STATS_INT_4] += 1
        elif ch == TYPE_INT_8:
            (_, index) = read_number(data, index, 2)
            stats[STATS_INT_8] += 1
        elif ch == TYPE_INT_16:
            (_, index) = read_number(data, index, 4)
            stats[STATS_INT_16] += 1
        elif ch == TYPE_INT_32:
            (_, index) = read_number(data, index, 8)
            stats[STATS_INT_32] += 1
        elif ch == TYPE_STRING_4:
            (size, index) = read_number(data, index, 1)
            index += size
            stats[STATS_STRING_4] += 1
        elif ch == TYPE_INT_8:
            (size, index) = read_number(data, index, 2)
            index += size
            stats[STATS_STRING_8] += 1
        elif ch == TYPE_STRING_16:
            (size, index) = read_number(data, index, 4)
            index += size
            stats[STATS_STRING_16] += 1
        elif ch == TYPE_STRING_32:
            (size, index) = read_number(data, index, 8)
            index += size
            stats[STATS_STRING_32] += 1
        elif ch == TYPE_STUFF_4:
            (_, index) = read_number(data, index, 1)
            stats[STATS_STUFF_4] += 1
        elif ch == TYPE_STUFF_8:
            (_, index) = read_number(data, index, 2)
            stats[STATS_STUFF_8] += 1
        elif ch == TYPE_INT_16:
            (_, index) = read_number(data, index, 4)
            stats[STATS_STUFF_16] += 1
        elif ch == TYPE_STUFF_32:
            (_, index) = read_number(data, index, 8)
            stats[STATS_STUFF_32] += 1

    if index != len(data):
        raise BagException(f'Not all input consumed at path="{path}"')

    return stats


def perform_huffman_coding(stats: dict) -> dict:
    if not stats:
        return {}

    heap = []
    uid = 0

    for type, freq in stats.items():
        uid += 1
        heapq.heappush(heap, (freq, uid, type))

    if len(heap) == 1:
        return {heap[0][2]: '0'}

    while len(heap) > 1:
        left_freq, _, left_types = heapq.heappop(heap)
        right_freq, _, right_types = heapq.heappop(heap)

        uid += 1
        heapq.heappush(heap, (left_freq + right_freq, uid, (left_types, right_types)))

    root = heap[0][2]
    codes = {}

    def assign_codes(node, code):
        if isinstance(node, tuple):
            assign_codes(node[0], code + '0')
            assign_codes(node[1], code + '1')
        else:
            codes[node] = code

    assign_codes(root, "")
    return codes


def main() -> int:
    bags_path = os.path.dirname(__file__) + '/paintings/bag-v1'
    total_stats = create_empty_stats()

    for root, _, files in os.walk(bags_path):
        for file in files:
            if file.endswith('.bpe'):
                path = os.path.join(root, file)
                print(f'Processing "{path}"...')

                stats = compute_stats(path)

                for key, value in stats.items():
                    total_stats[key] += value

    total_stats[STATS_STUFF_8] += 0.9
    total_stats[STATS_STRING_8] += 0.8
    total_stats[STATS_STUFF_16] += 0.7
    total_stats[STATS_INT_32] += 0.6
    total_stats[STATS_STUFF_32] += 0.5
    total_stats[STATS_STRING_16] += 0.4

    print()
    print('Stats:')

    for key, value in sorted(total_stats.items(), key=lambda x: x[1]):
        print(f'* {key}: {value}')

    huffman_codes = perform_huffman_coding(total_stats)

    print()
    print('Huffman:')

    for key, value in sorted(huffman_codes.items(), key=lambda x: len(x[1])):
        print(f'* {key}: {value}')

    return 0


if __name__ == '__main__':
    sys.exit(main())
