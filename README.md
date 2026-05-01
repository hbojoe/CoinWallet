# CoinWallet

CoinWallet is a hybrid physical economy plugin for Minecraft 1.21 that stores custom coins in a virtual wallet GUI.

## Features

- Three coin tiers:
  - Bronze coin (ID `10011`) value `1`
  - Silver coin (ID `10010`) value `64`
  - Gold coin (ID `10009`) value `4096`
- Automatic conversion:
  - `64` Bronze -> `1` Silver
  - `64` Silver -> `1` Gold
- Vault economy hook support.
- Vault hook can be disabled (default) so wallet is not treated as normal server money.
- SQL + YAML storage support.

## Commands

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/wallet` | `coinwallet.wallet` | Open your wallet |
| `/wallet [player]` | `coinwallet.wallet-others` | Open another player's wallet |
| `/walletbal` | `coinwallet.walletbal` | Check your wallet balance |
| `/walletbal [player]` | `coinwallet.walletbal-others` | Check another player's balance |
| `/wallettop` | `coinwallet.wallettop` | View leaderboard |
| `/coinwallet eco give <player> <bronze,silver,gold> <amount>` | `coinwallet.eco` | Add typed coins to a wallet |
| `/coinwallet eco take <player> <bronze,silver,gold> <amount>` | `coinwallet.eco` | Remove typed coins from a wallet |

## Configuration

```yaml
coins:
  bronze:
    id: 10011
    material: BRICK
  silver:
    id: 10010
    material: IRON_NUGGET
  gold:
    id: 10009
    material: GOLD_NUGGET
```

Set each `material` to the base material used by your ItemsAdder coins if you want fallback generation to match exactly.

## Notes

- Conversion is fixed at `64:1` between tiers:
  - `64` Bronze = `1` Silver
  - `64` Silver = `1` Gold
- `/walletbal` displays per-coin counts (gold/silver/bronze) and per-coin values instead of only a single total.
- Set `vault-hook.enabled: true` only if you explicitly want CoinWallet exposed as a Vault economy provider.

Default prefix: `[Coin Wallet]`

## Developer API

CoinWallet now exposes a Bukkit service: `me.hboj.api.CoinWalletAPI`.

In the plugin that wants to use CoinWallet as currency:

1. Add `CoinWallet` as a dependency in your `plugin.yml`
2. Get the API from the service manager and call it

```yaml
depend: [CoinWallet]
```

```java
import me.hboj.api.CoinWalletAPI;
import me.hboj.api.CoinWalletAPIProvider;
import org.bukkit.entity.Player;

public void charge(Player player, int bronzeValueCost) {
    CoinWalletAPI api = CoinWalletAPIProvider.get();
    if (api == null) {
        return; // CoinWallet not available
    }

    CoinWalletAPI.TransactionResult result = api.remove(player, bronzeValueCost);
    if (!result.isSuccess()) {
        // not enough balance or invalid request
        return;
    }

    // success
}
```

Useful methods:
- `getBalance(player)`
- `has(player, amount)`
- `getCurrencyValues()`
- `add(player, amount)`
- `remove(player, amount)`
- `setBalance(player, amount)`
- `addCoins/removeCoins(player, CoinUnit, amount)` where `CoinUnit` is `BRONZE`, `SILVER`, or `GOLD`

`getCurrencyValues()` returns:

```java
{
    "B": 1.0,
    "S": 64.0,
    "G": 4096.0
}
```


