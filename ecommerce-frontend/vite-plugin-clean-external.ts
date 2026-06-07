import type { Plugin } from 'vite'

export default function cleanExternalPlugin(): Plugin {
  return {
    name: 'clean-external',
    transform(code, id) {
      if (id.includes('node_modules')) {
        let cleanedCode = code
        
        cleanedCode = cleanedCode.replace(
          /fetch\s*\(\s*["']https?:\/\/[^"']*daxuesoutijiang[^"']*["']/g,
          'Promise.resolve(new Response(JSON.stringify({})))'
        )
        
        cleanedCode = cleanedCode.replace(
          /fetch\s*\(\s*["']https?:\/\/[^"']*hybridaction[^"']*["']/g,
          'Promise.resolve(new Response(JSON.stringify({})))'
        )
        
        cleanedCode = cleanedCode.replace(
          /new\s+XMLHttpRequest\s*\(\s*\)\s*\.\s*open\s*\(\s*["']POST["']\s*,\s*["']https?:\/\/[^"']*daxuesoutijiang[^"']*["']/g,
          '/* blocked by clean-external plugin */'
        )
        
        cleanedCode = cleanedCode.replace(
          /window\.fetch\s*=\s*function/g,
          '/* fetch override removed */'
        )
        
        if (cleanedCode !== code) {
          return {
            code: cleanedCode,
            map: null
          }
        }
      }
      
      return null
    }
  }
}
