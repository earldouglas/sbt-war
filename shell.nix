{ pkgs ? import <nixpkgs> {} }:
let
  jdk = pkgs.jdk11;

  nvim =
    let

      nvim-metals_lua =
        ''
          local map = vim.keymap.set

          local metals_config = require("metals").bare_config()

          metals_config.settings = {
            metalsBinaryPath = "${pkgs.metals}/bin/metals",

            -- autoImportBuild = "on",
            defaultBspToBuildTool = true,
            showImplicitArguments = true,
            showImplicitConversionsAndClasses = true,
            showInferredType = true,
            superMethodLensesEnabled = true,
          }

          metals_config.init_options.statusBarProvider = "off"

          metals_config.capabilities = require("cmp_nvim_lsp").default_capabilities()

          metals_config.on_attach = function(client, bufnr)

            -- LSP mappings
            map("n", "gD", vim.lsp.buf.definition)
            map("n", "K", vim.lsp.buf.hover)
            map("n", "gi", vim.lsp.buf.implementation)
            map("n", "gr", vim.lsp.buf.references)
            map("n", "gds", vim.lsp.buf.document_symbol)
            map("n", "gws", vim.lsp.buf.workspace_symbol)
            map("n", "<leader>cl", vim.lsp.codelens.run)
            map("n", "<leader>sh", vim.lsp.buf.signature_help)
            map("n", "<leader>rn", vim.lsp.buf.rename)
            map("n", "<leader>f", vim.lsp.buf.format)
            map("n", "<leader>ca", vim.lsp.buf.code_action)

            map("n", "<leader>ws", function()
              require("metals").hover_worksheet()
            end)

            -- all workspace diagnostics
            map("n", "<leader>aa", vim.diagnostic.setqflist)

            -- all workspace errors
            map("n", "<leader>ae", function()
              vim.diagnostic.setqflist({ severity = "E" })
            end)

            -- all workspace warnings
            map("n", "<leader>aw", function()
              vim.diagnostic.setqflist({ severity = "W" })
            end)

            -- buffer diagnostics only
            map("n", "<leader>d", vim.diagnostic.setloclist)

            map("n", "[c", function()
              vim.diagnostic.goto_prev({ wrap = false })
            end)

            map("n", "]c", function()
              vim.diagnostic.goto_next({ wrap = false })
            end)

          end

          local nvim_metals_group = vim.api.nvim_create_augroup("nvim-metals", { clear = true })

          vim.api.nvim_create_autocmd("FileType", {
            pattern = { "scala", "sbt", "java" },
            callback = function()
              require("metals").initialize_or_attach(metals_config)
            end,
            group = nvim_metals_group,
          })

          vim.opt_global.completeopt = { "menuone", "noinsert", "noselect" }

          require("fidget").setup {
            -- options
          }

          local cmp = require("cmp")
          cmp.setup({
            sources = {
              { name = "nvim_lsp" },
            },
            mapping = cmp.mapping.preset.insert({
              ["<CR>"] = cmp.mapping.confirm(),
              ["<Tab>"] = function(fallback)
                if cmp.visible() then
                  cmp.select_next_item()
                else
                  fallback()
                end
              end,
              ["<S-Tab>"] = function(fallback)
                if cmp.visible() then
                  cmp.select_prev_item()
                else
                  fallback()
                end
              end,
            }),
          })
        '';

      nvim-metals_vim =
        ''
          :lua << EOF
          ${nvim-metals_lua}
          EOF
        '';

      nerdtree_vim =
        ''
          nnoremap <C-,> :vertical resize -10<CR>
          nnoremap <C-.> :vertical resize +10<CR>
          nnoremap <C-f> :NERDTreeFind<CR>
          nnoremap <C-n> :NERDTree<CR>
          nnoremap <C-t> :NERDTreeToggle<CR>
          nnoremap <leader>n :NERDTreeFocus<CR>

          let g:NERDTreeShowHidden=1

          let g:NERDTreeRemoveDirCmd = '${pkgs.coreutils}/bin/rm -rf '
          let g:NERDTreeCopyCmd      = '${pkgs.coreutils}/bin/cp -r '
        '';

      vimrc =
        ''
          syntax on

          set autoindent
          set background=dark
          set backspace=indent,eol,start
          set clipboard=unnamedplus
          set colorcolumn=73
          set expandtab
          set mouse=
          set noswapfile
          set number
          set ruler
          set shiftwidth=2
          set termguicolors
          set textwidth=72

          colorscheme vim
        '';

      nvim_with_plugins =
        pkgs.neovim.override {
          configure = {
            customRC =
              builtins.concatStringsSep "\n" [
                nvim-metals_vim
                nerdtree_vim
                vimrc
              ];

            packages.myPlugins.start = [
              pkgs.vimPlugins.nvim-metals
              pkgs.vimPlugins.nerdtree
              pkgs.vimPlugins.fidget-nvim
              pkgs.vimPlugins.nvim-cmp
              pkgs.vimPlugins.cmp-nvim-lsp
            ];

          };
        };

    in 

      pkgs.runCommand "nvim" {
        buildInputs = [ pkgs.makeWrapper ];
      }
      ''
        mkdir -p $out/bin
        makeWrapper ${nvim_with_plugins}/bin/nvim $out/bin/nvim \
          --set PATH ${pkgs.lib.makeBinPath [
            jdk
            pkgs.bash
            pkgs.coreutils # kill
            pkgs.coursier
          ]}
      '';

in

  pkgs.mkShell {
    nativeBuildInputs = [
      (pkgs.sbt.override { jre = jdk; })
      jdk
      nvim
    ];
    shellHook = ''
      export JAVA_HOME=${jdk}
      PATH="${jdk}/bin:$PATH"
    '';
  }
