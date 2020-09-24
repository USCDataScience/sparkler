"""
Run Flask App in production context
"""

from waitress import serve
import run

serve(run.APP, host='0.0.0.0', port=5000)
