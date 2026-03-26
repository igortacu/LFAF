#!/usr/bin/env python3
"""
Generate PNG images from DOT files using graphviz Python library.
Install with: pip3 install graphviz
"""

try:
    from graphviz import Source
    import os

    # Change to graphs directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)

    # Generate NDFA graph
    print("Generating NDFA graph...")
    with open('ndfa_variant25.dot', 'r') as f:
        ndfa_source = Source(f.read())
        ndfa_source.render('ndfa_variant25', format='png', cleanup=True)
    print("✓ NDFA graph saved to: ndfa_variant25.png")

    # Generate DFA graph
    print("Generating DFA graph...")
    with open('dfa_variant25.dot', 'r') as f:
        dfa_source = Source(f.read())
        dfa_source.render('dfa_variant25', format='png', cleanup=True)
    print("✓ DFA graph saved to: dfa_variant25.png")

    print("\nBoth graphs generated successfully!")

except ImportError:
    print("Error: graphviz library not found.")
    print("Please install it with: pip3 install graphviz")
    print("\nAlternatively, if you have Graphviz installed, run:")
    print("  dot -Tpng ndfa_variant25.dot -o ndfa_variant25.png")
    print("  dot -Tpng dfa_variant25.dot -o dfa_variant25.png")
except Exception as e:
    print(f"Error: {e}")
