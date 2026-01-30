#!/bin/bash

# Script to convert Interview Rehearsal Guide to PDF
# Multiple options for PDF conversion

echo "🎯 Interview Rehearsal Guide - PDF Conversion"
echo "=========================================="
echo ""
echo "📄 Converting Interview Rehearsal Guide to PDF..."
echo ""

# Check if pandoc is available
if command -v pandoc &> /dev/null; then
    echo "✅ Using Pandoc for conversion..."
    pandoc "Interview_Rehearsal_Guide.md" \
           --toc \
           --toc-depth=3 \
           --number-sections \
           -V geometry:margin=0.75in \
           -V fontsize=11pt \
           -V documentclass=article \
           -V colorlinks=true \
           -V linkcolor=blue \
           -V urlcolor=blue \
           -V toccolor=blue \
           -o "Interview_Rehearsal_Guide.pdf"
    
    if [ $? -eq 0 ]; then
        echo "✅ PDF generated successfully: Interview_Rehearsal_Guide.pdf"
        echo ""
        echo "📚 Rehearsal Guide Features:"
        echo "   ✓ Quick facts and talking points"
        echo "   ✓ Whiteboard practice scenarios"
        echo "   ✓ Code examples to remember"
        echo "   ✓ Practice scripts for interviews"
        echo "   ✓ Mobile-friendly cheat sheet"
        echo "   ✓ Emergency quick reference"
        echo ""
        echo "🎯 Perfect for last-minute interview prep!"
    else
        echo "❌ Pandoc conversion failed"
    fi
else
    echo "❌ Pandoc not found. Please install pandoc:"
    echo ""
    echo "📥 Installation Options:"
    echo ""
    echo "Option 1: Install via Homebrew (macOS)"
    echo "   brew install pandoc"
    echo ""
    echo "Option 2: Install via MacPorts (macOS)"
    echo "   sudo port install pandoc"
    echo ""
    echo "Option 3: Download from official website"
    echo "   https://pandoc.org/installing.html"
    echo ""
    echo "Option 4: Use online converter"
    echo "   1. Copy the content from Interview_Rehearsal_Guide.md"
    echo "   2. Go to https://www.markdowntopdf.com/"
    echo "   3. Paste the content and convert to PDF"
    echo ""
    echo "Option 5: Use VS Code extension"
    echo "   1. Install 'Markdown PDF' extension in VS Code"
    echo "   2. Open the .md file"
    echo "   3. Right-click → 'Markdown PDF' → 'Export (pdf)'"
    echo ""
    
    echo "📋 Alternative: View in Browser"
    echo "   You can also view the markdown file directly in your browser:"
    echo "   1. Open Interview_Rehearsal_Guide.md"
    echo "   2. Use any markdown viewer extension or GitHub"
    echo ""
fi

echo ""
echo "🎭 Rehearsal Guide Sections:"
echo "   ✓ Quick Facts to Memorize"
echo "   ✓ Interview Talking Points"
echo "   ✓ Whiteboard Practice Scenarios"
echo "   ✓ Key Code Snippets to Remember"
echo "   ✓ Practice Scripts for Interviews"
echo "   ✓ Mobile-Friendly Cheat Sheet"
echo "   ✓ Emergency Quick Reference"
echo ""
echo "📊 Guide Statistics:"
echo "   • Comprehensive rehearsal material"
echo "   • Structured practice sections"
echo "   • Real interview scenarios"
echo "   • Code examples and talking points"
echo "   • Whiteboard drawing instructions"
echo "   • Quick reference numbers"
echo ""
echo "💡 Rehearsal Tips:"
echo "   • Practice drawing architecture on whiteboard"
echo "   • Rehearse project introduction (2-3 minutes)"
echo "   • Memorize key numbers and facts"
echo "   • Practice explaining technical challenges"
echo "   • Review code examples"
echo "   • Prepare scenario-based answers"
echo ""
echo "🚀 You're ready for your interview!"
