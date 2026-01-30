#!/bin/bash

# Script to convert Markdown to PDF for Interview Preparation Guide
# This script provides multiple options for PDF conversion

echo "🎯 Enterprise Inventory Management System - Interview Preparation"
echo "============================================================="
echo ""
echo "📄 Converting Markdown to PDF..."
echo ""

# Check if pandoc is available
if command -v pandoc &> /dev/null; then
    echo "✅ Using Pandoc for conversion..."
    pandoc "Enterprise_Inventory_System_Interview_Preparation.md" \
           --toc \
           --toc-depth=2 \
           --number-sections \
           -V geometry:margin=1in \
           -V fontsize=12pt \
           -V documentclass=article \
           -o "Enterprise_Inventory_System_Interview_Preparation.pdf"
    
    if [ $? -eq 0 ]; then
        echo "✅ PDF generated successfully: Enterprise_Inventory_System_Interview_Preparation.pdf"
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
    echo "   1. Copy the content from Enterprise_Inventory_System_Interview_Preparation.md"
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
    echo "   1. Open Enterprise_Inventory_System_Interview_Preparation.md"
    echo "   2. Use any markdown viewer extension or GitHub"
    echo ""
fi

echo ""
echo "📚 Interview Preparation Topics Covered:"
echo "   ✓ Architecture & Design Questions"
echo "   ✓ Spring Boot & Spring Cloud Questions"
echo "   ✓ Microservices Questions"
echo "   ✓ Database & JPA Questions"
echo "   ✓ Security & Authentication Questions"
echo "   ✓ API & REST Questions"
echo "   ✓ Testing Questions"
echo "   ✓ Docker & DevOps Questions"
echo "   ✓ Performance & Scalability Questions"
echo "   ✓ Scenario-Based Questions"
echo "   ✓ Advanced Technical Questions"
echo ""
echo "🎯 Total Questions: 24 main questions with 70+ cross-questions"
echo "📖 Content Length: ~15,000 words of comprehensive interview material"
echo ""
echo "💡 Tips for Interview:"
echo "   • Focus on understanding concepts, not memorizing"
echo "   • Practice explaining architecture on whiteboard"
echo "   • Be ready to discuss trade-offs and design decisions"
echo "   • Prepare code examples for key concepts"
echo "   • Review system design patterns and best practices"
echo ""
echo "🚀 Good luck with your interview!"
