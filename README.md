# RedstoneBridge
A spigot plugin to connect Minecraft redstone with real life lights.

# Setting up
You need some coding knowledge to set this up! The plugin is originally designed for my rgb control software (which is not public at the moment), so you will need an external http server to use it.

## Request Structure
Every registered block update will send an http request to the specified url for the block. The request contains the name of the bridge, mode of the bidge, powered state, power level, correponding color (set in the config).

Example request for RGB mode:
```json
{
  "name": "example-rgb",
  "mode": "RGB",
  "powered": true,
  "power-level": 1,
  "color": "#ff0000" 
}
```

Example request for SWITCH mode:
```json
{
  "name": "example-switch",
  "mode": "SWITCH",
  "powered": true,
}
```

## Example http server setup (with python and flask)
```py
# python 3.12
import flask

app = flask.Flask("example-server")

@app.route("/", methods=["POST"])
def bridge():
    request_json = flask.request.json
    bridge_name = request_json["name"]
    bridge_mode = request_json["mode"]
    block_powered = request_json["powered"]
    rgb = request_json["color"] if bridge_mode.lower() == "rgb" else None

    match bridge_name:
        case "example-switch":
            pass  # Send power command to LED
        case "example-rgb":
            pass  # Send power command and RGB data to LED
    return "OK", 200

# Run the server on localhost:8080
app.run(host="127.0.0.1", port=8080)  # You should use app.run() only if you are running this on a closed network
```

### Known issues
- Solid blocks do not fire a BlockRedstone event, so updates to those will not be detected.