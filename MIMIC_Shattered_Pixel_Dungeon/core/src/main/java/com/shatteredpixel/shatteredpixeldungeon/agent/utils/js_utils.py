import execjs
import subprocess

LOG_MSGs = {"bridge.mc_bot:log",
            "bridge.open_ai:log",
            "bot_action.codeGeneration:log",
            "bot_action.plan:log",
            "bot_action.summarize:log",
            "bot_action.planDecide:log",
            "bot_action.expect:log",
            "bot_action.codeDecide:log",
            "skill_library.SkillManager:log",
            "bot_action.codeDescription:log",
            "memory_stream.MemoryStream:log",
            "bot_action.preferenceAnalyze:log",
            "bot_action.planDecompose:log"}

CHAT_MSGs = {"bridge.mc_bot:chat"}

ERR_MSGs = {"bridge.mc_bot:error",
            "bridge.open_ai:error",
            "skill_library.SkillManager:error",
            "memory_stream.MemoryStream:error",
            "bot_action.preferenceAnalyze:error",
            "bot_action.planDecompose:error"}


def execjs_compile(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        js_text = f.read()

    return execjs.compile(js_text)


def subprocess_run_js(file_path, argv=None, capture_output=True, timeout=10):
    if argv is None:
        argv = []

    process = subprocess.run(
        ["node", file_path] + argv,
        capture_output=capture_output,
        text=capture_output,
        timeout=timeout
    )

    if capture_output:
        print('output: ', process.stdout)
        print('error: ', process.stderr)

    return process


def subprocess_popen_js(file_path, argv=None, capture_output=True):
    if argv is None:
        argv = []

    if capture_output:
        process = subprocess.Popen(
            ["node", file_path] + argv,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            bufsize=1,
            universal_newlines=True
        )

    else:
        process = subprocess.Popen(
            ["node", file_path] + argv
        )

    return process


def prGreen(string): print("\033[92m {}\033[00m".format(string), end="")


def prRed(string): print("\033[91m {}\033[00m".format(string), end="")


def prCyan(string): print("\033[96m {}\033[00m".format(string), end="")


def prYellow(string): print("\033[93m {}\033[00m".format(string), end="")


def print_log(log: str):
    """
    Print the colorful log according to the prefix
    :param log: The log to be printed
    :return: Void
    """
    for log_msg in LOG_MSGs:
        if log_msg in log:
            prGreen(log_msg)
            print(log.replace(log_msg, ''), end="")
            return

    for chat_msg in CHAT_MSGs:
        if chat_msg in log:
            prCyan(chat_msg)

            new_log = log.replace(chat_msg, '')
            user_name = new_log[1:new_log.find(":") + 1]
            prYellow(user_name)

            print(new_log[new_log.find(":") + 1:], end="")
            return

    for err_msg in ERR_MSGs:
        if err_msg in log:
            prRed(err_msg)
            print(log.replace(err_msg, ''), end="")
            return

    print(log, end="")
    return
